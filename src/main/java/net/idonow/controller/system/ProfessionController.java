package net.idonow.controller.system;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import net.idonow.common.api.ApiResponse;
import net.idonow.common.util.LocaleUtils;
import net.idonow.controller.mapping.ResponseMappers;
import net.idonow.entity.Profession;
import net.idonow.service.entity.ProfessionService;
import net.idonow.transform.profession.ProfessionRequest;
import net.idonow.transform.profession.ProfessionResponse;
import net.idonow.transform.profession.ProfessionUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController("systemProfessionController")
@RequestMapping("/system/professions")
public class ProfessionController {

    private final ProfessionService professionService;
    private final LocaleUtils localeUtils;
    private ResponseMappers responseMappers;

    public ProfessionController(ProfessionService professionService, LocaleUtils localeUtils) {
        this.professionService = professionService;
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

    // Mapping to create Profession
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Profession> addProfession(
            @Valid @RequestBody ProfessionRequest professionRequest) {

        Profession profession = professionService.createProfession(professionRequest);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.profession.added"), profession);
    }

    // Mapping to update existing Profession
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Profession> updateProfession(
            @Valid @RequestBody ProfessionUpdateRequest professionUpdateRequest) {

        Profession profession = professionService.updateProfession(professionUpdateRequest);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.profession.updated"), profession);
    }

    // Mapping to delete Profession
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProfession(@PathVariable("id") @Positive(message = "{validation.positive}") Long id) {

        professionService.deleteProfession(id);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.profession.deleted"));
    }
}
