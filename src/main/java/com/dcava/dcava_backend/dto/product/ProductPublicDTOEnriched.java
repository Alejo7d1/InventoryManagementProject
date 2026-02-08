package com.dcava.dcava_backend.dto.product;

import com.dcava.dcava_backend.dto.category.CategoryView;
import com.dcava.dcava_backend.model.Product;

public class ProductPublicDTOEnriched extends ProductPublicDTO {

    private final CategoryView categoryInfo;

    public ProductPublicDTOEnriched(Product product, CategoryView categoryInfo) {
        super(product);
        this.categoryInfo = categoryInfo;
    }

    public CategoryView getCategoryInfo() { return categoryInfo; }
}
