package net.idonow.controller;

import jakarta.validation.constraints.Positive;
import net.idonow.common.api.ApiResponse;
import net.idonow.common.util.LocaleUtils;
import net.idonow.controller.mapping.ResponseMappers;
import net.idonow.entity.ProfessionCategory;
import net.idonow.service.entity.ProfessionCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/profession-categories")
public class ProfessionCategoryController {

    private final ProfessionCategoryService professionCategoryService;
    private LocaleUtils localeUtils;
    private ResponseMappers responseMappers;

    public ProfessionCategoryController(ProfessionCategoryService professionCategoryService) {
        this.professionCategoryService = professionCategoryService;
    }

    @Autowired
    public void setLocaleUtils(LocaleUtils localeUtils) {
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
}
