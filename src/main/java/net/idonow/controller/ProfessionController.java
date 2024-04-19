package net.idonow.controller;

import jakarta.validation.constraints.Positive;
import net.idonow.common.api.ApiResponse;
import net.idonow.common.util.LocaleUtils;
import net.idonow.controller.mapping.ResponseMappers;
import net.idonow.entity.Profession;
import net.idonow.service.entity.ProfessionService;
import net.idonow.transform.profession.ProfessionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/professions")
public class ProfessionController {

    private final ProfessionService professionService;
    private LocaleUtils localeUtils;
    private ResponseMappers responseMappers;

    public ProfessionController(ProfessionService professionService) {
        this.professionService = professionService;
    }

    @Autowired
    public void setLocaleUtils(LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    @Autowired
    public void setResponseMappers(ResponseMappers responseMappers) {
        this.responseMappers = responseMappers;
    }

    // Mapping to get all Professions
    @GetMapping
    public ApiResponse<List<ProfessionResponse>> getProfessions() {

        List<Profession> professionList = professionService.getAllEntities();
        List<ProfessionResponse> responseList = professionList.stream()
                .map(responseMappers::professionToResponse)
                .toList();

        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entities.found"), responseList);
    }

    // Mapping to get professions by category
    @GetMapping(params = {"categoryId"})
    public ApiResponse<List<ProfessionResponse>> getProfessionsByCategory(
            @RequestParam("categoryId") @Positive(message = "{validation.positive}") Long categoryId) {

        List<Profession> professionList = professionService.getEntitiesByCategory(categoryId);
        List<ProfessionResponse> responseList = professionList.stream()
                .map(responseMappers::professionToResponse)
                .toList();

        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entities.found"), responseList);
    }

    // Mapping to get specific Profession
    @GetMapping("{id}")
    public ApiResponse<Profession> getProfession(@PathVariable("id") @Positive(message = "{validation.positive}") Long id) {

        Profession profession = professionService.getEntity(id);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"), profession);
    }
}
