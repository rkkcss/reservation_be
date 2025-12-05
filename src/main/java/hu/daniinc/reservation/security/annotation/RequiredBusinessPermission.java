package hu.daniinc.reservation.security.annotation;

import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.domain.enumeration.BusinessRole;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiredBusinessPermission {
    BusinessPermission value();

    String businessIdParam() default "businessId";
}
