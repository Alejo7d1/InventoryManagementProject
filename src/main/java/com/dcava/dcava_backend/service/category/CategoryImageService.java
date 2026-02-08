package com.dcava.dcava_backend.service.category;

import com.dcava.dcava_backend.dto.category.CategoryView;
import com.dcava.dcava_backend.model.Category;
import com.dcava.dcava_backend.repository.CategoryRepository;
import com.dcava.dcava_backend.config.AppProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Locale;

@Service
public class CategoryImageService {

    private final CategoryRepository categoryRepository;
    private final S3Client r2Client;
    private final AppProperties appProperties;

    public CategoryImageService(
            CategoryRepository categoryRepository,
            S3Client r2Client,
            AppProperties appProperties
    ) {
        this.categoryRepository = categoryRepository;
        this.r2Client = r2Client;
        this.appProperties = appProperties;
    }

    public CategoryView uploadCategoryImage(Integer categoryId, MultipartFile file) throws IOException {
        // Find category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Validate status
        if ("inactive".equalsIgnoreCase(category.getStatus())) {
            throw new IllegalArgumentException("This category is inactive");
        }

        // Validate file is image
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Selected file is not an image");
        }

        // Read bytes
        byte[] bytes = file.getBytes();

        // Ensure slug exists (or generate one)
        String slug = category.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = slugify(category.getName());
            category.setSlug(slug);
        }

        // Build key path in R2
        String fileName = "category_" + categoryId + ".png";
        String r2Key = "categories/" + slug + "/" + fileName;

        // Upload to R2
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(appProperties.getR2().getBucket())
                    .key(r2Key)
                    .contentType(contentType)
                    .contentLength((long) bytes.length)
                    .build();

            r2Client.putObject(request, RequestBody.fromBytes(bytes));
        } catch (S3Exception e) {
            throw new RuntimeException("Error uploading category image to R2: "
                    + e.awsErrorDetails().errorMessage(), e);
        }

        // Save DB path
        String dbPath = "/" + r2Key;
        category.setImageUrl(dbPath);

        Category saved = categoryRepository.save(category);

        return new CategoryView(saved.getId(), saved.getName(), saved.getSlug(),
                saved.getDescription(), toPublicUrl(saved.getImageUrl()));
    }

    public CategoryView getCategoryViewWithPublicImage(Integer categoryId) {
        Category c = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return new CategoryView(c.getId(), c.getName(), c.getSlug(),
                c.getDescription(), toPublicUrl(c.getImageUrl()));
    }

    private String toPublicUrl(String dbPath) {
        if (dbPath == null || dbPath.isBlank()) return null;

        String publicUrl = appProperties.getR2().getPublicUrl().trim();
        String bucket = appProperties.getR2().getBucket().trim();

        String path = dbPath.trim();
        String key = path.startsWith("/") ? path.substring(1) : path;

        if (publicUrl.endsWith("/")) return publicUrl + bucket + "/" + key;
        return publicUrl + "/" + key;
    }

    private String slugify(String input) {
        return input.toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9\\-]", "");
    }
}
