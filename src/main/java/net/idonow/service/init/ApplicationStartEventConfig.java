package net.idonow.service.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.TimeZone;

@SuppressWarnings("All")
@Slf4j
@Component
public class ApplicationStartEventConfig {

    private final OnStartupService onStartupService;
    private final CacheEvictingService cacheEvictingService;

    public ApplicationStartEventConfig(OnStartupService onStartupService,
                                       CacheEvictingService cacheEvictingService) {
        this.onStartupService = onStartupService;
        this.cacheEvictingService = cacheEvictingService;
    }

    @EventListener(condition = "@environment.getProperty('spring.jpa.hibernate.ddl-auto') == 'validate'")
    public void validateDatasource(@NonNull ApplicationReadyEvent applicationReadyEvent) {
        onStartupService.validateACL();
        log.info("Static data successfully validated");
    }

    @EventListener
    public void evictCaches(@NonNull ApplicationReadyEvent event) {
        // Evict all caches except token's
        cacheEvictingService.evictAllCaches();
        log.info("Necessary caches evicted");
    }

    @EventListener(condition = "@environment.getProperty('spring.profiles.active') == 'dev'")
    public void setTimezone(@NonNull ApplicationReadyEvent event) {

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        log.info("Setting server timezone: " + TimeZone.getDefault().getID());

    }
}
