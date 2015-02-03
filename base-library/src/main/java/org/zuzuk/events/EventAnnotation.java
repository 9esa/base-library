package org.zuzuk.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventAnnotation {
    String value() default "";

    boolean isGlobalBroadcast() default false;

    boolean isOnlyWhileResumed() default true;
}