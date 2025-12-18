package hu.daniinc.reservation.security.aspect;

import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.repository.BusinessEmployeeRepository;
import hu.daniinc.reservation.security.annotation.RequiredBusinessPermission;
import hu.daniinc.reservation.web.rest.errors.GeneralException;
import java.util.Arrays;
import java.util.Set;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BusinessPermissionAspect {

    private final BusinessEmployeeRepository businessEmployeeRepository;

    public BusinessPermissionAspect(BusinessEmployeeRepository businessEmployeeRepository) {
        this.businessEmployeeRepository = businessEmployeeRepository;
    }

    @Before("@annotation(required)")
    public void checkPermission(JoinPoint jp, RequiredBusinessPermission required) {
        String paramName = required.businessIdParam();
        Long businessId = extractBusinessId(jp, paramName);

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

    private Long extractBusinessId(JoinPoint jp, String paramName) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = jp.getArgs();

        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(paramName)) {
                Object arg = args[i];

                if (paramName.equals("businessEmployeeId") && arg instanceof Long) {
                    return businessEmployeeRepository
                        .findById((Long) arg)
                        .map(be -> be.getBusiness().getId())
                        .orElseThrow(() -> new GeneralException("BusinessEmployee not found", "not-found", HttpStatus.NOT_FOUND));
                }

                return (Long) arg;
            }
        }

        throw new RuntimeException("Parameter '" + paramName + "' not found");
    }
}
