package net.idonow.controller.system;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import net.idonow.common.api.ApiResponse;
import net.idonow.common.util.LocaleUtils;
import net.idonow.controller.mapping.ResponseMappers;
import net.idonow.entity.ProfessionCategory;
import net.idonow.service.entity.ProfessionCategoryService;
import net.idonow.transform.profession.category.ProfessionCategoryRequest;
import net.idonow.transform.profession.category.ProfessionCategoryUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@Validated
@RestController("systemProfessionCategoryController")
@RequestMapping("/system/profession-categories")
public class ProfessionCategoryController {

    private final ProfessionCategoryService professionCategoryService;
    private final LocaleUtils localeUtils;
    private ResponseMappers responseMappers;

    public ProfessionCategoryController(ProfessionCategoryService professionCategoryService, LocaleUtils localeUtils) {
        this.professionCategoryService = professionCategoryService;
        this.localeUtils = localeUtils;
    }

    @Autowired
    public void setResponseMappers(ResponseMappers responseMappers) {
        this.responseMappers = responseMappers;
    }

    // Mapping to get all ProfessionCategories
    @GetMapping
    public ApiResponse<Object> getProfessionCategories(
            @RequestParam(value = "structure", required = false) String structure) {
        List<ProfessionCategory> categoryList = professionCategoryService.getAllEntities();
        if ("tree".equals(structure)) {
            return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entities.found"), responseMappers.professionCategoryListToResponseTree(new LinkedList<>(categoryList)));
        }
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entities.found"), categoryList);
    }

    // Mapping to get specific ProfessionCategory
    @GetMapping("{id}")
    public ApiResponse<ProfessionCategory> getProfessionCategory(@PathVariable("id") @Positive(message = "{validation.positive}") Long id) {
        ProfessionCategory professionCategory = professionCategoryService.getEntity(id);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"), professionCategory);
    }

    // Mapping to create new ProfessionCategory
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> addProfessionCategory(
            @Valid @RequestBody ProfessionCategoryRequest categoryRequest,
            @RequestParam(value = "sendUpdatedTree", required = false) boolean sendUpdatedTree) {

        ProfessionCategory professionCategory = professionCategoryService.createCategory(categoryRequest);

        // Choose response data type (either updated object or updated tree)
        Object responseData = sendUpdatedTree
                ? responseMappers.professionCategoryListToResponseTree(new LinkedList<>(professionCategoryService.getAllEntities()))
                : professionCategory;

        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.profession.category.added"), responseData);
    }

    // Mapping to update existing ProfessionCategory
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> updateProfessionCategory(
            @Valid @RequestBody ProfessionCategoryUpdateRequest categoryUpdateRequest,
            @RequestParam(value = "sendUpdatedTree", required = false) boolean sendUpdatedTree) {
        ProfessionCategory professionCategory = professionCategoryService.updateCategory(categoryUpdateRequest);
        // Choose response data type (either updated object or updated tree)
        Object responseData = sendUpdatedTree
                ? responseMappers.professionCategoryListToResponseTree(new LinkedList<>(professionCategoryService.getAllEntities()))
                : professionCategory;

        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.profession.category.updated"), responseData);
    }

    // Mapping to delete ProfessionCategory
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> deleteProfessionCategory(
            @PathVariable("id") @Positive(message = "{validation.positive}") Long id,
            @RequestParam(value = "sendUpdatedTree", required = false) boolean sendUpdatedTree) {
        professionCategoryService.deleteCategory(id);
        // Choose response data type (either updated object or updated tree)
        Object responseData = sendUpdatedTree
                ? responseMappers.professionCategoryListToResponseTree(new LinkedList<>(professionCategoryService.getAllEntities()))
                : null;

        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.profession.category.deleted"), responseData);
    }

}
