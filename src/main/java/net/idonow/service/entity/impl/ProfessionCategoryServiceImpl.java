package net.idonow.service.entity.impl;

import lombok.extern.slf4j.Slf4j;
import net.idonow.common.util.LocaleUtils;
import net.idonow.controller.exception.common.ActionNotAllowedException;
import net.idonow.controller.exception.common.EntityNotFoundException;
import net.idonow.controller.exception.common.InvalidRequestDataException;
import net.idonow.entity.ProfessionCategory;
import net.idonow.repository.ProfessionCategoryRepository;
import net.idonow.service.entity.ProfessionCategoryService;
import net.idonow.transform.profession.category.ProfessionCategoryRequest;
import net.idonow.transform.profession.category.ProfessionCategoryUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.idonow.common.cache.EntityCacheNames.ALL_PROFESSION_CATEGORIES;
import static net.idonow.common.cache.EntityCacheNames.PROFESSION_CATEGORY;

@Slf4j
@Service
public class ProfessionCategoryServiceImpl implements ProfessionCategoryService {

    private final ProfessionCategoryRepository professionCategoryRepository;
    private LocaleUtils localeUtils;

    public ProfessionCategoryServiceImpl(ProfessionCategoryRepository professionCategoryRepository) {
        this.professionCategoryRepository = professionCategoryRepository;
    }

    @Autowired
    public void setLocaleUtils(LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    @Override
    @Cacheable(value = ALL_PROFESSION_CATEGORIES)
    public List<ProfessionCategory> getAllEntities() {
        return professionCategoryRepository.findAll();
    }

    @Override
    @Cacheable(value = PROFESSION_CATEGORY, key = "#id", unless = "#result == null")
    public ProfessionCategory getEntity(Long id) {
        Optional<ProfessionCategory> optProfessionCategory = professionCategoryRepository.findById(id);
        if (optProfessionCategory.isEmpty()) {
            throw new EntityNotFoundException(String.format("Profession category with id {%d} not found", id));
        }
        return optProfessionCategory.get();
    }

    @Override
    @CacheEvict(value = ALL_PROFESSION_CATEGORIES, allEntries = true)
    public ProfessionCategory createCategory(ProfessionCategoryRequest categoryRequest) {
        ProfessionCategory professionCategory = new ProfessionCategory();
        // Checking the existence of parent if its id is passed
        if (categoryRequest.getParentId() != null) {
            ProfessionCategory parentCategory = this.getEntity(categoryRequest.getParentId());
            professionCategory.setParent(parentCategory);
        }
        professionCategory.setCategoryName(categoryRequest.getCategoryName());
        professionCategoryRepository.save(professionCategory);

        log.info("Profession category successfully created: {}", professionCategory);
        return professionCategory;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = PROFESSION_CATEGORY, allEntries = true),
            @CacheEvict(value = ALL_PROFESSION_CATEGORIES, allEntries = true)
    })
    public ProfessionCategory updateCategory(ProfessionCategoryUpdateRequest categoryUpdateRequest) {
        ProfessionCategory professionCategory = this.getEntity(categoryUpdateRequest.getId());

        // Check if requested to update parent category
        if (categoryUpdateRequest.getParentId() != null) {
            if (categoryUpdateRequest.getId().equals(categoryUpdateRequest.getParentId())) {
                throw new InvalidRequestDataException(
                        "Profession category id and parent id must be different",
                        Map.of("parentId", localeUtils.getLocalizedMessage("error.category.self-reference")));
            }
            // Check if parent needs to be updated
            if (professionCategory.getParent() == null || !professionCategory.getParent().getId().equals(categoryUpdateRequest.getParentId())) {
                ProfessionCategory parentCategory = this.getEntity(categoryUpdateRequest.getParentId());
                if (this.parentIsSelfOrDescendant(new LinkedList<>(this.getAllEntities()), professionCategory, parentCategory)) {
                    throw new ActionNotAllowedException("Parent profession category cannot be from his children");
                }
                professionCategory.setParent(parentCategory);
            }
        } else {
            if (professionCategory.getParent() != null) {
                professionCategory.setParent(null);
            }
        }

        professionCategory.setCategoryName(categoryUpdateRequest.getCategoryName());
        professionCategoryRepository.save(professionCategory);

        log.info("Profession category successfully updated: {}", professionCategory);

        return professionCategory;

    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = PROFESSION_CATEGORY, allEntries = true),
            @CacheEvict(value = ALL_PROFESSION_CATEGORIES, allEntries = true)
    })
    public void deleteCategory(Long id) {

        professionCategoryRepository.deleteById(id);
        log.info("Profession category successfully deleted: {id = {}}", id);
    }

    /* -- PRIVATE METHODS -- */

    private boolean parentIsSelfOrDescendant(List<ProfessionCategory> categoryList, ProfessionCategory node, ProfessionCategory parent) {
        if (node.equals(parent)) {
            return true;
        }

        List<ProfessionCategory> childCategories = new LinkedList<>();

        // Get child categories and compare with parent
        for (ProfessionCategory category : new LinkedList<>(categoryList)) {
            if (category.getParent() != null && category.getParent().equals(node)) {
                if (category.equals(parent)) {
                    return true;
                }
                childCategories.add(category);
                categoryList.remove(category);
            }
        }

        // If no children - return false
        if (!childCategories.isEmpty()) {
            for (ProfessionCategory childNode : childCategories) {
                // Compare children RECURSIVELY
                if (parentIsSelfOrDescendant(categoryList, childNode, parent)) {
                    return true;
                }
            }
        }
        return false;
    }
}
