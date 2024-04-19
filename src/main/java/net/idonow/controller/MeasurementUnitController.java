package net.idonow.controller;

import jakarta.validation.constraints.Positive;
import net.idonow.common.api.ApiResponse;
import net.idonow.common.util.LocaleUtils;
import net.idonow.entity.MeasurementUnit;
import net.idonow.service.entity.MeasurementUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/measurement-units")
public class MeasurementUnitController {

    private final MeasurementUnitService measurementUnitService;

    private LocaleUtils localeUtils;

    public MeasurementUnitController(MeasurementUnitService measurementUnitService) {
        this.measurementUnitService = measurementUnitService;
    }

    @Autowired
    public void setLocaleUtils(LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    // Mapping to get all MeasurementUnits
    @GetMapping
    public ApiResponse<List<MeasurementUnit>> getMeasurementsUnits() {
        List<MeasurementUnit> measurementUnitList = measurementUnitService.getAllEntities();
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entities.found"), measurementUnitList);
    }

    // Mapping to get specific MeasurementUnit by id
    @GetMapping("{id}")
    public ApiResponse<MeasurementUnit> getMeasurementUnit(@PathVariable("id") @Positive(message = "{validation.positive}") Long id) {
        MeasurementUnit measurementUnit = measurementUnitService.getEntity(id);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"), measurementUnit);
    }
}
