package hu.daniinc.reservation.service;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.service.dto.BusinessDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    private final BusinessService businessService;

    public TenantFilter(BusinessService businessService) {
        this.businessService = businessService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        String host = request.getServerName(); // "pizzeria-bella.localhost"
        String slug = extractSlug(host);

        if (slug != null) {
            try {
                BusinessDTO business = businessService.findBySlug(slug);
                request.setAttribute("tenantBusinessId", business.getId());
            } catch (Exception e) {
                // unknown slug → 404
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Business not found");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String extractSlug(String host) {
        // lokális fejlesztéshez: X-Tenant-ID header fallback
        // (ezt csak fejlesztés közben használd)
        String[] parts = host.split("\\.");
        if (parts.length >= 3) {
            return parts[0]; // "pizzeria-bella"
        }
        return null;
    }
}
