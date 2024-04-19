package net.idonow.service.entity.impl;

import lombok.extern.slf4j.Slf4j;
import net.idonow.controller.exception.common.EntityNotFoundException;
import net.idonow.entity.MeasurementUnit;
import net.idonow.repository.MeasurementUnitRepository;
import net.idonow.service.entity.MeasurementUnitService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static net.idonow.common.cache.EntityCacheNames.ALL_MEASUREMENTS;
import static net.idonow.common.cache.EntityCacheNames.MEASUREMENT;

@Slf4j
@Service
public class MeasurementUnitServiceImpl implements MeasurementUnitService {

    private final MeasurementUnitRepository measurementUnitRepository;

    public MeasurementUnitServiceImpl(MeasurementUnitRepository measurementUnitRepository) {
        this.measurementUnitRepository = measurementUnitRepository;
    }

    @Override
    @Cacheable(value = ALL_MEASUREMENTS)
    public List<MeasurementUnit> getAllEntities() {
        return measurementUnitRepository.findAll();
    }

    @Override
    @Cacheable(value = MEASUREMENT, key = "#id", unless = "#result == null")
    public MeasurementUnit getEntity(Long id) {
        Optional<MeasurementUnit> optMeasurementUnit = measurementUnitRepository.findById(id);
        if (optMeasurementUnit.isEmpty()) {
            throw new EntityNotFoundException(String.format("MeasurementUnit with id {%d} not found", id));
        }
        return optMeasurementUnit.get();
    }
}
