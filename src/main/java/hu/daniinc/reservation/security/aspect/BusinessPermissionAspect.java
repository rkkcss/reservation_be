package hu.daniinc.reservation.security.aspect;

import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.repository.BusinessEmployeeRepository;
import hu.daniinc.reservation.security.annotation.RequiredBusinessPermission;
import hu.daniinc.reservation.service.BusinessEmployeeService;
import hu.daniinc.reservation.service.UserService;
import hu.daniinc.reservation.service.dto.BusinessEmployeeDTO;
import hu.daniinc.reservation.web.rest.errors.GeneralException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
public class BusinessPermissionAspect {

    private final BusinessEmployeeRepository businessEmployeeRepository;
    private final UserService userService;
    private final BusinessEmployeeService businessEmployeeService;
    private final HttpServletRequest request;

    public BusinessPermissionAspect(
        BusinessEmployeeRepository businessEmployeeRepository,
        UserService userService,
        BusinessEmployeeService businessEmployeeService,
        HttpServletRequest request
    ) {
        this.businessEmployeeRepository = businessEmployeeRepository;
        this.userService = userService;
        this.businessEmployeeService = businessEmployeeService;
        this.request = request;
    }

    @Before("@annotation(required)")
    public void checkPermission(JoinPoint jp, RequiredBusinessPermission required) {
        // Már nem passzoljuk át a paramName-t, mert a request-ből dolgozunk
        Long businessId = extractBusinessId();

        if (businessId == null) {
            throw new GeneralException(
                "Business ID cannot be null for permission checking.",
                "business-id-cant-null",
                HttpStatus.BAD_REQUEST
            );
        }

        BusinessEmployee employee = businessEmployeeRepository
            .findByUserLoginAndBusinessId(businessId)
            .orElseThrow(() ->
                new GeneralException("You are not an employee of this business.", "employee-not-part-of-business", HttpStatus.BAD_REQUEST)
            );

        Set<BusinessPermission> employeePerms = employee.getPermissions();
        BusinessPermission[] requiredPerms = required.value();
        boolean requireAll = required.requiredAll();

        boolean authorized;

        // if all permissions required
        if (requireAll) {
            authorized = Arrays.stream(requiredPerms).allMatch(employeePerms::contains);
        }
        // if only one permission required
        else {
            authorized = Arrays.stream(requiredPerms).anyMatch(employeePerms::contains);
        }

        if (!authorized) {
            throw new GeneralException(
                "Missing required permission(s): " + Arrays.toString(requiredPerms),
                "permission-missing",
                HttpStatus.BAD_REQUEST
            );
        }
    }

    private Long extractBusinessId() {
        Long businessId = (Long) request.getAttribute("tenantBusinessId");
        if (businessId != null) {
            return businessId;
        }

        String businessHeader = request.getHeader("X-Business-ID");
        if (businessHeader != null) {
            Long requestedId = Long.parseLong(businessHeader);

            // Is user belongs to business validation
            User user = userService
                .getUserWithAuthorities()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            BusinessEmployeeDTO employee = businessEmployeeService.findByBusinessIdAndUserId(requestedId, user.getId());

            if (employee == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
            }

            request.setAttribute("tenantBusinessId", requestedId);
            return requestedId;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No business context");
    }
}
