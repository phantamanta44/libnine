package io.github.phantamanta44.libnine.util.nullity;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Documented
public @interface Reflected {
    // NO-OP
}
