package com.diode.lilypadoc.standard.api;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigTitle {
    String value() default "";
}
