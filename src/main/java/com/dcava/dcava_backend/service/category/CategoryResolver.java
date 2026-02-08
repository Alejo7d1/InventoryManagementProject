package com.dcava.dcava_backend.service.category;

import com.dcava.dcava_backend.dto.category.CategoryAdminView;
import com.dcava.dcava_backend.dto.category.CategoryView;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CategoryResolver {
    CategoryView resolveOne(String legacyCategoryName);
    List<CategoryAdminView> getAllEnrichedIncludeInactive();
    Map<String, CategoryView> resolveAll(Collection<String> legacyCategoryNames);
    Map<String, CategoryView> getAllEnriched();
}
