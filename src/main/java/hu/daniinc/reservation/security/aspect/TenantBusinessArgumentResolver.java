package hu.daniinc.reservation.security.aspect;

import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.security.annotation.TenantBusiness;
import hu.daniinc.reservation.service.BusinessEmployeeService;
import hu.daniinc.reservation.service.UserService;
import hu.daniinc.reservation.service.dto.BusinessEmployeeDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

/**
 * Feloldja a {@code @TenantBusiness} annotációval jelölt Long paramétereket.
 *
 * A businessId forrása prioritási sorrendben:
 * 1. A request attribútumban tárolt "tenantBusinessId" - FONTOS: ezt az értéket
 *    kizárólag egy megelőzően lefutó, már validált interceptor/filter töltheti fel
 *    (pl. amely már ellenőrizte, hogy a user jogosult az adott business-hez).
 *    Ez a resolver itt NEM végez újabb jogosultság-ellenőrzést ezen az ágon.
 * 2. Az "X-Business-ID" header - ebben az esetben a resolver maga ellenőrzi,
 *    hogy a bejelentkezett user az adott business alkalmazottja-e.
 */
@Component
public class TenantBusinessArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String REQUEST_ATTRIBUTE = "tenantBusinessId";
    private static final String HEADER_NAME = "X-Business-ID";

    private final UserService userService;
    private final BusinessEmployeeService businessEmployeeService;

    public TenantBusinessArgumentResolver(UserService userService, BusinessEmployeeService businessEmployeeService) {
        this.userService = userService;
        this.businessEmployeeService = businessEmployeeService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(TenantBusiness.class) && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        TenantBusiness annotation = parameter.getParameterAnnotation(TenantBusiness.class);
        boolean isRequired = annotation == null || annotation.required();

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new IllegalStateException("TenantBusinessArgumentResolver csak servlet környezetben használható");
        }

        Long attributeBusinessId = (Long) request.getAttribute(REQUEST_ATTRIBUTE);
        if (attributeBusinessId != null) {
            return attributeBusinessId;
        }

        String businessHeader = request.getHeader(HEADER_NAME);
        if (businessHeader != null) {
            return resolveFromHeader(businessHeader, isRequired);
        }

        if (isRequired) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No business context");
        }
        return null;
    }

    private Long resolveFromHeader(String businessHeader, boolean isRequired) {
        Long requestedId;
        try {
            requestedId = Long.parseLong(businessHeader);
        } catch (NumberFormatException e) {
            if (isRequired) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Business ID format");
            }
            return null;
        }

        User user = userService
            .getUserWithAuthorities()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        BusinessEmployeeDTO employee = businessEmployeeService.findByBusinessIdAndUserId(requestedId, user.getId());

        if (employee == null) {
            if (isRequired) {
                // A user autentikált, de nem jogosult ehhez a business-hez - ez authorization hiba, nem "not found".
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not an employee of this business");
            }
            return null;
        }

        return requestedId;
    }
}
