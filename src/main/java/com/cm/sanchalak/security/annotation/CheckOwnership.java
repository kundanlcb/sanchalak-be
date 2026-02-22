package com.cm.sanchalak.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark service methods that require school ownership validation.
 * The aspect will intercept these methods and ensure the current user (if not
 * platform admin)
 * belongs to the same school as the resource being modified.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckOwnership {
    /**
     * The type of resource being checked (e.g., "STUDENT", "TEACHER").
     * Used for descriptive error messages.
     */
    String value() default "RESOURCE";
}
