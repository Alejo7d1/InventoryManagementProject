package com.dcava.dcava_backend.dto.product;

import com.dcava.dcava_backend.model.ProductImage;

public class ProductImageDTO {
    private int id;
    private String fileName;
    private String filePath;

    public ProductImageDTO(ProductImage image) {
        this.id = image.getId();
        this.fileName = image.getFileName();
        this.filePath = image.getFilePath();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}


