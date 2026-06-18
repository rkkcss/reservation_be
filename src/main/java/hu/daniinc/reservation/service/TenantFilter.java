package hu.daniinc.reservation.service;

import hu.daniinc.reservation.service.dto.BusinessDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(TenantFilter.class);
    private final BusinessService businessService;
    private final TenantSlugExtractorService tenantSlugExtractorService;

    public TenantFilter(BusinessService businessService, TenantSlugExtractorService tenantSlugExtractorService) {
        this.businessService = businessService;
        this.tenantSlugExtractorService = tenantSlugExtractorService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        String host = request.getServerName();
        String slug = tenantSlugExtractorService.extractTenantSlug(host);

        BusinessDTO businessDTO = null;

        try {
            if (slug != null) {
                businessDTO = businessService.findBySlug(slug);
            } else {
                //TODO:: domain from environment
                if (!host.equals("localhost") && !host.equals("booklyapp.me") && !host.equals("www.booklyapp.me")) {
                    businessDTO = businessService.findByCustomDomain(host);
                }
            }
        } catch (Exception e) {
            LOG.error("Hiba történt a business lekérése közben. Host: {}", host, e);
        }

        if (businessDTO != null) {
            request.setAttribute("tenantBusinessId", businessDTO.getId());
        } else {
            request.setAttribute("tenantBusinessId", null);
        }

        chain.doFilter(request, response);
    }
}
