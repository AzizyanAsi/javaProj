package net.idonow.service.entity.impl;

import lombok.extern.slf4j.Slf4j;
import net.idonow.controller.exception.common.EntityNotFoundException;
import net.idonow.entity.Country;
import net.idonow.repository.CountryRepository;
import net.idonow.service.entity.CountryService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static net.idonow.common.cache.EntityCacheNames.ALL_COUNTRIES;
import static net.idonow.common.cache.EntityCacheNames.COUNTRY;

@Slf4j
@Service
public class CountryServiceImpl implements CountryService {
    private final CountryRepository countryRepository;

    public CountryServiceImpl(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Override
    @Cacheable(value = ALL_COUNTRIES)
    public List<Country> getAllEntities() {
        return countryRepository.findAll();
    }

    @Override
    public Country getEntity(Long id) {
        Optional<Country> optCountry = countryRepository.findById(id);
        if (optCountry.isEmpty()) {
            throw new EntityNotFoundException(String.format("Country with id {%d} not found", id));
        }
        return optCountry.get();
    }

    @Override
    @Cacheable(value = COUNTRY, key = "#code", unless = "#result == null")
    public Country getByCode(String code) {
        Optional<Country> optCountry = countryRepository.findByCountryCode(code);
        if (optCountry.isEmpty()) {
            throw new EntityNotFoundException(String.format("Country with code {%s} not found", code));
        }
        return optCountry.get();
    }
}
