package com.dcava.dcava_backend.service.advertisement;

import com.dcava.dcava_backend.config.AppProperties;
import com.dcava.dcava_backend.model.Advertisement;
import com.dcava.dcava_backend.model.Advertisement.AdType;
import com.dcava.dcava_backend.repository.AdvertisementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@Service
public class AdvertisementService {

    private static final Logger log = LoggerFactory.getLogger(AdvertisementService.class);

    private final AdvertisementRepository advertisementRepository;
    private final S3Client r2Client;
    private final AppProperties appProperties;

    private static final String ADS_DIR = "ads";

    public AdvertisementService(
            AdvertisementRepository advertisementRepository,
            S3Client r2Client,
            AppProperties appProperties
    ) {
        this.advertisementRepository = advertisementRepository;
        this.r2Client = r2Client;
        this.appProperties = appProperties;
    }

    public Advertisement createAdvertisement(
            MultipartFile file,
            String title,
            AdType adType,
            String linkUrl
    ) throws IOException {

        validateImageDimensions(file, adType);

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Selected file is not an image");
        }

        // normaliza link (opcional)
        String normalizedLink = normalizeOptionalUrl(linkUrl);

        byte[] bytes = file.getBytes();

        String originalFilename = file.getOriginalFilename();
        String extension = ".png";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID() + extension;
        String r2Key = ADS_DIR + "/" + filename;

        try {
            r2Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(appProperties.getR2().getBucket())
                            .key(r2Key)
                            .contentType(contentType)
                            .contentLength((long) bytes.length)
                            .build(),
                    RequestBody.fromBytes(bytes)
            );
            log.info("Advertisement image uploaded to R2: key={} sizeBytes={} type={}",
                    r2Key, bytes.length, adType);
        } catch (S3Exception e) {
            log.error("R2 upload failed: bucket={} key={} error={}",
                    appProperties.getR2().getBucket(), r2Key,
                    e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException(
                    "Error uploading advertisement to R2: " +
                            e.awsErrorDetails().errorMessage(),
                    e
            );
        }

        Advertisement ad = new Advertisement();
        ad.setTitle(title);
        ad.setAdType(adType);
        ad.setFilePath("/" + r2Key);
        ad.setLinkUrl(normalizedLink); // puede ser null

        Advertisement saved = advertisementRepository.save(ad);
        log.info("Advertisement created: id={} title={} type={}", saved.getId(), title, adType);
        return saved;
    }

    private String normalizeOptionalUrl(String linkUrl) {
        if (linkUrl == null) return null;

        String trimmed = linkUrl.trim();
        if (trimmed.isEmpty()) return null;

        // only http/https
        try {
            URI uri = URI.create(trimmed);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Invalid link URL scheme (only http/https allowed)");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("Invalid link URL host");
            }
            return trimmed;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid link URL: " + trimmed);
        }
    }

    public List<Advertisement> getAllAdvertisements() {
        List<Advertisement> ads = advertisementRepository.findAllByOrderByCreatedAtDesc();

        String publicUrl = appProperties.getR2().getPublicUrl();

        for (Advertisement ad : ads) {
            String path = ad.getFilePath();

            if (path == null || path.isBlank()) continue;
            if (path.startsWith("http")) continue;

            String fullUrl = publicUrl + path;
            ad.setFilePath(fullUrl);
        }
        return ads;
    }


    public List<Advertisement> getAdvertisementsByType(AdType adType) {
        List<Advertisement> ads = advertisementRepository.findByAdType(adType);

        String publicUrl = appProperties.getR2().getPublicUrl();

        for (Advertisement ad : ads) {
            String path = ad.getFilePath();

            if (path == null || path.isBlank()) continue;
            if (path.startsWith("http")) continue;

            String fullUrl = publicUrl + path;

            ad.setFilePath(fullUrl);
        }
        return ads;
    }


    public Advertisement getAdvertisementById(Integer id) {
        Advertisement ad = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        String path = ad.getFilePath();

        if (path != null && !path.isBlank() && !path.startsWith("http")) {
            String publicUrl = appProperties.getR2().getPublicUrl();

            String fullUrl = publicUrl + path;
            ad.setFilePath(fullUrl);
        }
        return ad;
    }


    public void deleteAdvertisement(Integer id) {
        Advertisement ad = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found"));
        try {
            String key = getString(ad);
            r2Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(appProperties.getR2().getBucket())
                            .key(key)
                            .build()
            );
            log.info("Advertisement deleted from R2: id={} key={}", id, key);
        } catch (S3Exception e) {
            log.error("R2 delete failed: bucket={} key={} error={}",
                    appProperties.getR2().getBucket(), ad.getFilePath(),
                    e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException(
                    "Error deleting advertisement from R2: " +
                            e.awsErrorDetails().errorMessage(),
                    e
            );
        }
        advertisementRepository.delete(ad);
        log.info("Advertisement deleted: id={} title={}", id, ad.getTitle());
    }

    private String getString(Advertisement ad) {
        String filePath = ad.getFilePath();
        // Extract R2 key
        String key;

        if (filePath.startsWith("http")) {
            String publicUrl = appProperties.getR2().getPublicUrl();
            String bucket = appProperties.getR2().getBucket();

            key = filePath
                    .replace(publicUrl, "")
                    .replace("/" + bucket, "")
                    .replaceFirst("^/", "");
        } else {
            // Ej: /ads/file.png
            key = filePath.replaceFirst("^/", "");
        }
        return key;
    }


    //Validation
    private void validateImageDimensions(MultipartFile file, AdType adType) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("Invalid image format");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        switch (adType) {
            case BANNER:
                if (width < 1024) {
                    throw new IllegalArgumentException(
                            "The banner must be at least 1024px wide. Actual: " + width + "px"
                    );
                }
                if (height < 200 || height > 800) {
                    throw new IllegalArgumentException(
                            "The banner should be between 200-800px high. Actual: " + height + "px"
                    );
                }
                break;

            case SIDEBAR:
                if (width != 1200) {
                    throw new IllegalArgumentException(
                            "The sidebar must be exactly 1200px wide. Actual: " + width + "px"
                    );
                }
                if (height < 1000 || height > 3000) {
                    throw new IllegalArgumentException(
                            "The sidebar should be between 1000-3000px high. Actual: " + height + "px"
                    );
                }
                break;

            case SQUARE:
                if (width < 600 || width > 2000 || height < 600 || height > 2000) {
                    throw new IllegalArgumentException(
                            "The square should be between 600-2000px on each side. Actual: " +
                                    width + "x" + height
                    );
                }
                double ratio = (double) width / height;
                if (ratio < 0.9 || ratio > 1.1) {
                    throw new IllegalArgumentException(
                            "The square should be approximately 1:1. Current ratio: " +
                                    String.format("%.2f", ratio)
                    );
                }
                break;

            case POPUP:
                if (width < 600 || width > 2000) {
                    throw new IllegalArgumentException(
                            "The popup should be between 600-2000px wide. Actual: " + width + "px"
                    );
                }
                if (height < 400 || height > 2000) {
                    throw new IllegalArgumentException(
                            "The popup should be between 400-2000px high. Actual: " + height + "px"
                    );
                }
                break;
        }
    }
}