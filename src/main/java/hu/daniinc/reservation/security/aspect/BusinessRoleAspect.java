package hu.daniinc.reservation.security.aspect;

import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.repository.BusinessEmployeeRepository;
import hu.daniinc.reservation.security.annotation.RequireBusinessRole;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BusinessRoleAspect {

    private final BusinessEmployeeRepository employeeRepository;

    public BusinessRoleAspect(BusinessEmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Before("@annotation(requireBusinessRole)")
    public void checkRole(JoinPoint joinPoint, RequireBusinessRole requireBusinessRole) throws AccessDeniedException {
        Long businessId = getaLong(joinPoint, requireBusinessRole);

        BusinessEmployee employee = employeeRepository
            .findByUserLoginAndBusinessId(businessId)
            .orElseThrow(() -> new AccessDeniedException("No business employee found for this business"));

        boolean allowed = Arrays.asList(requireBusinessRole.value()).contains(employee.getRole());
        if (!allowed) {
            throw new AccessDeniedException("You don't have the required role for this business");
        }
    }

    private static Long getaLong(JoinPoint joinPoint, RequireBusinessRole requireBusinessRole) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();

        Long businessId = null;
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(requireBusinessRole.businessIdParam())) {
                businessId = (Long) args[i];
                break;
            }
        }
        if (businessId == null) {
            throw new IllegalArgumentException("BusinessId parameter not found in method arguments");
        }
        return businessId;
    }
}
