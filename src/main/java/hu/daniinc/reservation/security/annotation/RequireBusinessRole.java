package hu.daniinc.reservation.security.annotation;

import hu.daniinc.reservation.domain.enumeration.BusinessRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireBusinessRole {
    BusinessRole[] value();

    String businessIdParam() default "businessId";
}
