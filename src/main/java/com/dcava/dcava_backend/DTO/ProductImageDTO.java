package com.dcava.dcava_backend.DTO;

import com.dcava.dcava_backend.model.ProductImage;

public class ProductImageDTO {
    private int id;
    private String fileName;
    private String filePath;

    public ProductImageDTO(ProductImage pi) {
        this.id = pi.getId();
        this.fileName = pi.getFileName();
        this.filePath = pi.getFilePath();
    }
}

