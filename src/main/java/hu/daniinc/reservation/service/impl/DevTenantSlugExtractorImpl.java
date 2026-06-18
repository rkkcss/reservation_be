package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.service.TenantSlugExtractorService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.jhipster.config.JHipsterConstants;

@Service
@Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
public class DevTenantSlugExtractorImpl implements TenantSlugExtractorService {

    @Override
    public String extractTenantSlug(String host) {
        String[] parts = host.split("\\.");

        if (parts.length == 2 && parts[1].equals("localhost") && !parts[0].equals("localhost")) {
            return parts[0];
        }
        return null;
    }
}
