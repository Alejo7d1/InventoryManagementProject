package com.dcava.dcava_backend.service.product;

import com.dcava.dcava_backend.config.AppProperties;
import com.dcava.dcava_backend.model.Product;
import com.dcava.dcava_backend.model.ProductImage;
import com.dcava.dcava_backend.repository.ProductImageRepository;
import com.dcava.dcava_backend.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.List;

@Service
public class ProductImageService {

    private static final Logger log = LoggerFactory.getLogger(ProductImageService.class);

    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final S3Client r2Client;
    private final AppProperties appProperties;

    public ProductImageService(
            ProductImageRepository imageRepository,
            ProductRepository productRepository,
            S3Client r2Client,
            AppProperties appProperties
    ) {
        this.imageRepository = imageRepository;
        this.productRepository = productRepository;
        this.r2Client = r2Client;
        this.appProperties = appProperties;
    }

    //Get images by product
    public List<ProductImage> getImagesByProduct(Integer productId) {
        List<ProductImage> images = imageRepository.findByProductId(productId);

        String publicUrl = appProperties.getR2().getPublicUrl().trim();
        String bucket = appProperties.getR2().getBucket().trim();

        for (ProductImage img : images) {

            if ("default.png".equals(img.getFileName())) {
                continue;
            }

            String path = img.getFilePath().trim();

            String fullUrl =
                    publicUrl.endsWith("/")
                            ? publicUrl + bucket + "/" + path
                            : publicUrl + path;

            img.setFilePath(fullUrl);
        }

        return images;
    }



    //Save image
    public ProductImage saveImage(Integer productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Validate
        if ("inactive".equals(product.getStatus())) {
            throw new IllegalArgumentException("This product is inactive");
        }

        // get byte array
        byte[] bytes = file.getBytes();

        // validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Selected file is not an image");
        }

        String category = product.getCategory() != null ? product.getCategory() : "uncategorized";

        // temporal save in db
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setFileName("temp");
        image.setFilePath("temp");
        ProductImage savedImage = imageRepository.save(image);

        // get real id
        int imageId = savedImage.getId();

        // generate name
        String newFileName = "image_" + imageId + "_product_" + productId + "." + "png";
        String r2Key = category + "/" + newFileName;

        // upload to R2
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(appProperties.getR2().getBucket())
                    .key(r2Key)
                    .contentType(contentType)
                    .contentLength((long) bytes.length)
                    .build();

            r2Client.putObject(request, RequestBody.fromBytes(bytes));
            log.info("Product image uploaded to R2: imageId={} productId={} key={} sizeBytes={}",
                    imageId, productId, r2Key, bytes.length);

        } catch (S3Exception e) {
            log.error("R2 upload failed: imageId={} productId={} bucket={} key={} error={}",
                    imageId, productId, appProperties.getR2().getBucket(), r2Key,
                    e.awsErrorDetails().errorMessage(), e);
            // if failed, delete the temp register
            imageRepository.delete(savedImage);
            throw new RuntimeException("Error uploading image to R2: " + e.awsErrorDetails().errorMessage(), e);
        }

        // Update image with real information
        savedImage.setFileName(newFileName);
        savedImage.setFilePath("/" + r2Key);

        return imageRepository.save(savedImage);
    }

    //Generic image
    @Transactional
    public void saveGenericImage(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        boolean exists = imageRepository.findByProductId(productId)
                .stream()
                .anyMatch(img -> "image.png".equals(img.getFileName()));

        if (exists) return;

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setFileName("image.png");
        image.setFilePath("/default/image.png");

        imageRepository.save(image);
    }


    //Delete image
    public boolean deleteImage(Integer imageId) {
        return imageRepository.findById(imageId).map(image -> {

            // Default image cannot be eliminated
            if ("image.png".equals(image.getFileName())) {
                imageRepository.delete(image);
                return true;
            }

            // delete from R2
            String key = image.getFilePath().startsWith("/") ? image.getFilePath().substring(1) : image.getFilePath();
            try {
                r2Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(appProperties.getR2().getBucket())
                        .key(key)
                        .build());
                log.info("Product image deleted from R2: imageId={} key={}", imageId, key);
            } catch (S3Exception e) {
                log.error("R2 delete failed: imageId={} bucket={} key={} error={}",
                        imageId, appProperties.getR2().getBucket(), key,
                        e.awsErrorDetails().errorMessage(), e);
                throw new RuntimeException("Error deleting image from R2: " + e.awsErrorDetails().errorMessage(), e);
            }

            // delete from db
            imageRepository.delete(image);
            return true;

        }).orElseThrow(() -> new RuntimeException("Image not found"));
    }
}
