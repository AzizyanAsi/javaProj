package net.idonow.service.common.impl;

import net.idonow.common.config.AppConfig;
import net.idonow.common.config.LimitsConfig;
import net.idonow.common.data.CountryCode;
import net.idonow.controller.mapping.ResponseMappers;
import net.idonow.entity.Country;
import net.idonow.service.common.SystemConfigService;
import net.idonow.service.entity.CountryService;
import net.idonow.transform.country.CountryConfigResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import static net.idonow.common.util.PhoneNumberUtils.getPhoneNumberData;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    private final AppConfig appConfig;
    private final LimitsConfig limitsConfig;
    private final CountryService countryService;
    private final ResponseMappers responseMappers;

    public SystemConfigServiceImpl(AppConfig appConfig, LimitsConfig limitsConfig,
                                   CountryService countryService, ResponseMappers responseMappers) {
        this.appConfig = appConfig;
        this.limitsConfig = limitsConfig;
        this.countryService = countryService;
        this.responseMappers = responseMappers;
    }

    @Override
    public Map<String, Object> getPublicConfig() {
        // Server/system config
        Map<String, Object> publicConfig = new LinkedHashMap<>();
        publicConfig.put("system", getSystemConfig());
        publicConfig.put("application", getApplicationConfig());
        return publicConfig;
    }

    /* -- PRIVATE METHODS -- */
    private Map<String, Object> getSystemConfig() {
        // Server/system config
        Map<String, Object> system = new LinkedHashMap<>();
        system.put("time", LocalDateTime.now());
        TimeZone timezone = TimeZone.getDefault();
        ZoneOffset offset = timezone.toZoneId().getRules().getStandardOffset(Instant.now());
        system.put("timezone", Map.of(
                "id", timezone.getID(),
                "name", timezone.getDisplayName(),
                "offset", Map.of(
                        "id", offset.getId(),
                        "seconds", offset.getTotalSeconds())));
        return system;
    }

    private Map<String, Object> getApplicationConfig() {
        // Prepare country config
        Country country = countryService.getByCode(appConfig.getAppCountryCode());
        CountryConfigResponse countryCfgResp = responseMappers.countryToConfigResponse(country);
        CountryCode countryCode;
        if (countryCfgResp != null) {
            try {
                countryCode = CountryCode.valueOf(country.getCountryCode());
                countryCfgResp.setPhoneNumberData(getPhoneNumberData(countryCode));
            } catch (IllegalArgumentException ignore) {
            }
        }

        // Application config
        Map<String, Object> applicationConfig = new LinkedHashMap<>();
        applicationConfig.put("version", appConfig.getVersion());
        applicationConfig.put("country", countryCfgResp);
        applicationConfig.put("language", appConfig.getAppDefaultLanguage());
        applicationConfig.put("resendTokenAfterSeconds", limitsConfig.getResendTokenAfterSeconds());
        return applicationConfig;
    }

}
