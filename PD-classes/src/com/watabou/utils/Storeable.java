package com.watabou.utils;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.FIELD)
public @interface Storeable {
    Class enumClass() default Enum.class;

    /**
     * Ignore errors while restoring field.
     * @return boolean
     */
    boolean ignoreExceptions() default false;

    /**
     * Field won't be changed if no key presents in bundle.
     * @return boolean
     */
    boolean ignoreNull() default true;
}
