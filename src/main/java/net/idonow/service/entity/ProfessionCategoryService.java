package net.idonow.service.entity;

import net.idonow.entity.ProfessionCategory;
import net.idonow.service.entity.templates.EntityReadService;
import net.idonow.transform.profession.category.ProfessionCategoryRequest;
import net.idonow.transform.profession.category.ProfessionCategoryUpdateRequest;

public interface ProfessionCategoryService extends EntityReadService<ProfessionCategory> {

    ProfessionCategory createCategory(ProfessionCategoryRequest categoryRequest);

    ProfessionCategory updateCategory(ProfessionCategoryUpdateRequest categoryUpdateRequest);

    void deleteCategory(Long id);
}
