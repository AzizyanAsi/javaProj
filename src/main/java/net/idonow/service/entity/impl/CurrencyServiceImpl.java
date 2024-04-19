package net.idonow.service.entity.impl;

import lombok.extern.slf4j.Slf4j;
import net.idonow.controller.exception.common.EntityNotFoundException;
import net.idonow.entity.Currency;
import net.idonow.repository.CurrencyRepository;
import net.idonow.service.entity.CurrencyService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static net.idonow.common.cache.EntityCacheNames.ALL_CURRENCIES;
import static net.idonow.common.cache.EntityCacheNames.CURRENCY;

@Slf4j
@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    public CurrencyServiceImpl(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Override
    @Cacheable(value = ALL_CURRENCIES)
    public List<Currency> getAllEntities() {
        return currencyRepository.findAll();
    }

    @Override
    @Cacheable(value = CURRENCY, key = "#id", unless = "#result == null")
    public Currency getEntity(Long id) {
        Optional<Currency> optCurrency = currencyRepository.findById(id);
        if (optCurrency.isEmpty()) {
            throw new EntityNotFoundException(String.format("Currency with id {%d} not found", id));
        }
        return optCurrency.get();
    }
}
