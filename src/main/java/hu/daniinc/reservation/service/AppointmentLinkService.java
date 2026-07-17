package hu.daniinc.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import tech.jhipster.config.JHipsterProperties;

@Service
public class AppointmentLinkService {

    private final JHipsterProperties jHipsterProperties;

    public AppointmentLinkService(JHipsterProperties jHipsterProperties) {
        this.jHipsterProperties = jHipsterProperties;
    }

    public String generateModificationLinkWithQueryParam(String slug, String modifierToken) {
        String baseUrl = jHipsterProperties.getMail().getBaseUrl();

        return UriComponentsBuilder.fromUriString(baseUrl)
            .host(slug + "." + UriComponentsBuilder.fromUriString(baseUrl).build().getHost())
            .pathSegment("appointment", modifierToken)
            .toUriString();
    }
}
