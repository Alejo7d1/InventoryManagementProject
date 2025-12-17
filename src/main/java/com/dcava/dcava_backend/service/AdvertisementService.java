package com.dcava.dcava_backend.service;

import com.dcava.dcava_backend.model.Advertisement;
import com.dcava.dcava_backend.model.Advertisement.AdType;
import com.dcava.dcava_backend.repository.AdvertisementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class AdvertisementService {

    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private static final String ADS_SUBDIR = "ads";

    public Advertisement createAdvertisement(MultipartFile file, String title, AdType adType) throws IOException {
        // validate dimensions
        validateImageDimensions(file, adType);

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".png";
        String filename = UUID.randomUUID().toString() + extension;

        // create directory (if doesnt exist)
        Path adsPath = Paths.get(uploadDir, ADS_SUBDIR);
        if (!Files.exists(adsPath)) {
            Files.createDirectories(adsPath);
        }

        // save archive
        Path filePath = adsPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // create register in DB
        Advertisement ad = new Advertisement();
        ad.setFilePath("/uploads/" + ADS_SUBDIR + "/" + filename);
        ad.setTitle(title);
        ad.setAdType(adType);

        return advertisementRepository.save(ad);
    }

    public List<Advertisement> getAllAdvertisements() {
        return advertisementRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Advertisement> getAdvertisementsByType(AdType adType) {
        return advertisementRepository.findByAdType(adType);
    }

    public Advertisement getAdvertisementById(Integer id) {
        return advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found"));
    }


    public void deleteAdvertisement(Integer id) throws IOException {
        Advertisement ad = getAdvertisementById(id);

        // delete image
        String relativePath = ad.getFilePath().replace("/uploads/", "");
        Path filePath = Paths.get(uploadDir, relativePath);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // delete register in DB
        advertisementRepository.delete(ad);
    }

    private void validateImageDimensions(MultipartFile file, AdType adType) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("invalid image format");
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
                            "The sidebar must be exactly 300px wide. Actual: " + height + "px"
                    );
                }
                break;

            case SIDEBAR:
                if (width != 300) {
                    throw new IllegalArgumentException(
                            "The sidebar must be exactly 300px wide. Actual: " + width + "px"
                    );
                }
                if (height < 250 || height > 600) {
                    throw new IllegalArgumentException(
                            "The sidebar should be between 250-600px high. Actual: " + height + "px"
                    );
                }
                break;

            case SQUARE:
                if (width < 600 || width > 2000) {
                    throw new IllegalArgumentException(
                            "The square should be between 600-2000px wide. Actual: " + width + "px"
                    );
                }
                if (height < 600 || height > 2000) {
                    throw new IllegalArgumentException(
                            "The square should be between 600-2000px high. Actual: " + height + "px"
                    );
                }
                // Validate aspect-ratio
                double ratio = (double) width / height;
                if (ratio < 0.9 || ratio > 1.1) {
                    throw new IllegalArgumentException(
                            "The square should be approximately square (1:1). Current ratio: " +
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