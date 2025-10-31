package com.rsps.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark API handler classes with their endpoint path
 * Use this to automatically register handlers via reflection
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ApiEndpoint {
    /**
     * The endpoint path (e.g., "/give-item")
     */
    String value();
}
