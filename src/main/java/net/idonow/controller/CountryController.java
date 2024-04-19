package net.idonow.controller;

import net.idonow.common.api.ApiResponse;
import net.idonow.common.config.AppConfig;
import net.idonow.common.util.LocaleUtils;
import net.idonow.controller.mapping.ResponseMappers;
import net.idonow.entity.Country;
import net.idonow.entity.Currency;
import net.idonow.service.entity.CountryService;
import net.idonow.transform.country.CountryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    private final CountryService countryService;
    private AppConfig appConfig;
    private LocaleUtils localeUtils;
    private ResponseMappers responseMappers;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Autowired
    public void setLocaleUtils(LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    @Autowired
    public void setResponseMappers(ResponseMappers responseMappers) {
        this.responseMappers = responseMappers;
    }

    @GetMapping
    public ApiResponse<List<CountryResponse>> getCountries() {
        List<CountryResponse> countries = countryService.getAllEntities().stream().map(c -> responseMappers.countryToResponse(c)).collect(Collectors.toList());
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entities.found"), countries);
    }

    @GetMapping("default")
    public ApiResponse<CountryResponse> getDefaultCountry() {
        Country country = countryService.getByCode(appConfig.getAppCountryCode());
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.country.default"), responseMappers.countryToResponse(country));
    }

    @GetMapping("{code}")
    public ApiResponse<CountryResponse> getCountry(@PathVariable("code") String code) {
        // Country code path variable is case-insensitive (find uppercase only)
        Country country = countryService.getByCode(code.toUpperCase());
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"), responseMappers.countryToResponse(country));
    }

    @GetMapping("default/currency")
    public ApiResponse<Currency> getCurrencyOfDefaultCountry() {
        Currency currency = countryService.getByCode(appConfig.getAppCountryCode()).getCurrency();
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"), currency);
    }

    @GetMapping("{code}/currency")
    public ApiResponse<Currency> getCurrency(@PathVariable("code") String code) {
        Currency currency = countryService.getByCode(code.toUpperCase()).getCurrency();
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"), currency);
    }

}
