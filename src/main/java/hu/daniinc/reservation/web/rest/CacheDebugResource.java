package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.security.AuthoritiesConstants;
import jakarta.annotation.security.RolesAllowed;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class CacheDebugResource {

    private final Logger log = LoggerFactory.getLogger(CacheDebugResource.class);
    private final CacheManager cacheManager;

    public CacheDebugResource(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * GET /api/admin/cache-debug : Lekéri a business gyorsítótárak aktuális tartalmát.
     * Csak ADMIN szerepkörrel érhető el!
     */
    /**
     * GET /api/admin/all-caches : Lekéri az ÖSSZES Caffeine gyorsítótár nevét és tartalmát.
     */
    @GetMapping("/all-caches")
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<Map<String, Map<String, Object>>> getAllCaches() {
        log.debug("REST request to dump ALL Caches");

        Map<String, Map<String, Object>> allCachesMap = new HashMap<>();

        // A Spring Bootban regisztrált összes cache nevén végigmegyünk
        for (String cacheName : cacheManager.getCacheNames()) {
            Map<String, Object> cacheEntries = new HashMap<>();
            org.springframework.cache.Cache springCache = cacheManager.getCache(cacheName);

            if (springCache != null) {
                try {
                    @SuppressWarnings("unchecked")
                    javax.cache.Cache<Object, Object> nativeCache = (javax.cache.Cache<Object, Object>) springCache.getNativeCache();

                    nativeCache.forEach(entry -> {
                        if (entry.getKey() != null) {
                            cacheEntries.put(entry.getKey().toString(), entry.getValue());
                        }
                    });
                } catch (Exception e) {
                    cacheEntries.put("__error__", "Nem lehetett kiolvasni a cache tartalmát: " + e.getMessage());
                }
            }
            allCachesMap.put(cacheName, cacheEntries);
        }

        return ResponseEntity.ok(allCachesMap);
    }

    /**
     * Segédmetódus, ami térképpé alakítja a Caffeine natív cache tartalmát
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getCacheEntries(String cacheName) {
        Map<String, Object> entries = new HashMap<>();
        org.springframework.cache.Cache springCache = cacheManager.getCache(cacheName);

        if (springCache != null) {
            // Mivel JHipster + Caffeine páros van, a getNativeCache() egy javax.cache.Cache-t ad vissza
            javax.cache.Cache<Object, Object> nativeCache = (javax.cache.Cache<Object, Object>) springCache.getNativeCache();

            // Bejárjuk a cache elemeit és kimentjük a kulcs-érték párokat
            nativeCache.forEach(entry -> {
                // A kulcs a slug/host String, az érték a BusinessDTO lesz
                entries.put(entry.getKey().toString(), entry.getValue());
            });
        }

        return entries;
    }
}
