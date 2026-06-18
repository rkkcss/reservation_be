package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.service.TenantSlugExtractorService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.jhipster.config.JHipsterConstants;

@Service
@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
public class TenantSlugExtractor implements TenantSlugExtractorService {

    @Override
    public String extractTenantSlug(String host) {
        String[] parts = host.split("\\.");

        if (parts.length >= 3) {
            String subdomain = parts[0];
            if (!subdomain.equalsIgnoreCase("www") && !subdomain.equalsIgnoreCase("admin")) {
                return subdomain;
            }
        }
        return null;
    }
}
