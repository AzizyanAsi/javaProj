package net.idonow.controller;

import jakarta.validation.constraints.Positive;
import net.idonow.common.api.ApiResponse;
import net.idonow.common.util.LocaleUtils;
import net.idonow.entity.Currency;
import net.idonow.service.entity.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    private LocaleUtils localeUtils;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Autowired
    public void setLocaleUtils(LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    // Mapping to get all Currencies
    @GetMapping
    public ApiResponse<List<Currency>> getCurrencies() {
        List<Currency> currencies = currencyService.getAllEntities();
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entities.found"), currencies);
    }

    // Mapping to get specific Currency by id
    @GetMapping("{id}")
    public ApiResponse<Currency> getCurrency(@PathVariable("id") @Positive(message = "{validation.positive}") Long id) {
        Currency currency = currencyService.getEntity(id);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"), currency);
    }
}
