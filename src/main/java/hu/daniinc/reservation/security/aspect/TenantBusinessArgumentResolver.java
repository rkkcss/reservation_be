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

@Component
public class TenantBusinessArgumentResolver implements HandlerMethodArgumentResolver {

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
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        Long businessId = (Long) request.getAttribute("tenantBusinessId");
        if (businessId != null) return businessId;

        String businessHeader = request.getHeader("X-Business-ID");
        if (businessHeader != null) {
            Long requestedId = Long.parseLong(businessHeader);

            // is user belongs to business
            User user = userService
                .getUserWithAuthorities()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            BusinessEmployeeDTO employee = businessEmployeeService.findByBusinessIdAndUserId(requestedId, user.getId());

            if (employee == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
            }
            return requestedId;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No business context");
    }
}
