package net.idonow.service.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CacheEvictingService {

    private CacheManager cacheManager;

    @Autowired
    public void setCacheManager(RedisCacheManager redisCacheManager) {
        this.cacheManager = redisCacheManager;
    }

    public void evictAllCaches() {
        // Cache manager returns immutable collection
        List<String> cacheNames = new ArrayList<>(cacheManager.getCacheNames());
        for (String cn : cacheNames) {
            Cache cache = cacheManager.getCache(cn);
            if (cache != null) {
                cache.clear();
            }
        }
    }

}
