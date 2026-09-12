package hu.daniinc.reservation.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantBusiness {
    /**
     * If true, and no businessId, throw an error (400 / Exception).
     * If false, and no businessId than return null.
     */
    boolean required() default true;
}
