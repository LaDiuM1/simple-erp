package io.github.ladium1.erp.global.audit;

import io.github.ladium1.erp.global.menu.Menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    Menu menu();
    AuditAction action();
    String targetType() default "";
    String targetIdParam() default "";
    boolean targetIdFromReturn() default false;
}
