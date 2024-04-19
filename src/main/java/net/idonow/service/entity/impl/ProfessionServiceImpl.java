package net.idonow.service.entity.impl;

import lombok.extern.slf4j.Slf4j;
import net.idonow.controller.exception.common.EntityNotFoundException;
import net.idonow.entity.Profession;
import net.idonow.entity.ProfessionCategory;
import net.idonow.repository.ProfessionRepository;
import net.idonow.service.entity.ProfessionCategoryService;
import net.idonow.service.entity.ProfessionService;
import net.idonow.transform.profession.ProfessionRequest;
import net.idonow.transform.profession.ProfessionUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static net.idonow.common.cache.EntityCacheNames.ALL_PROFESSIONS;
import static net.idonow.common.cache.EntityCacheNames.PROFESSION;

@Slf4j
@Service
public class ProfessionServiceImpl implements ProfessionService {

    private final ProfessionRepository professionRepository;
    private ProfessionCategoryService professionCategoryService;

    public ProfessionServiceImpl(ProfessionRepository professionRepository) {
        this.professionRepository = professionRepository;
    }

    @Autowired
    public void setProfessionCategoryService(ProfessionCategoryService professionCategoryService) {
        this.professionCategoryService = professionCategoryService;
    }

    @Override
    @Cacheable(value = ALL_PROFESSIONS)
    public List<Profession> getAllEntities() {
        return professionRepository.findAll();
    }

    @Override
    @Cacheable(value = PROFESSION, key = "#id", unless = "#result == null")
    public Profession getEntity(Long id) {
        Optional<Profession> optProfession = professionRepository.findById(id);
        if (optProfession.isEmpty()) {
            throw new EntityNotFoundException(String.format("Profession with id {%d} not found", id));
        }
        return optProfession.get();
    }

    @Override
    public List<Profession> getEntitiesByCategory(Long categoryId) {
        return professionRepository.findByProfessionCategory_Id(categoryId);
    }

    @Override
    @CacheEvict(value = ALL_PROFESSIONS, allEntries = true)
    public Profession createProfession(ProfessionRequest professionRequest) {
        // If profession category with passed id does not exist throws exception
        ProfessionCategory professionCategory = professionCategoryService.getEntity(professionRequest.getProfessionCategoryId());

        Profession profession = new Profession();
        profession.setProfessionCategory(professionCategory);
        profession.setProfessionName(professionRequest.getProfessionName());

        professionRepository.save(profession);
        log.info("Profession successfully created: {}", professionCategory);

        return profession;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = PROFESSION, key = "#result.id"),
            @CacheEvict(value = ALL_PROFESSIONS, allEntries = true)
    })
    public Profession updateProfession(ProfessionUpdateRequest professionUpdateRequest) {

        Profession profession = this.getEntity(professionUpdateRequest.getId());
        profession.setProfessionName(professionUpdateRequest.getProfessionName());

        // Check if category needs to be updated
        if (!profession.getProfessionCategory().getId().equals(professionUpdateRequest.getCategoryId())) {
            ProfessionCategory profCategory = professionCategoryService.getEntity(professionUpdateRequest.getCategoryId());
            profession.setProfessionCategory(profCategory);
        }

        professionRepository.save(profession);
        log.info("Profession successfully updated: {}", profession);

        return profession;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = PROFESSION, key = "#id"),
            @CacheEvict(value = ALL_PROFESSIONS, allEntries = true)
    })
    public void deleteProfession(Long id) {
        professionRepository.deleteById(id);
        log.info("Profession successfully deleted: {id = {}}", id);
    }
}
