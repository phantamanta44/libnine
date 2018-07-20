package io.github.phantamanta44.libnine.util.nullity;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierNickname;
import javax.annotation.meta.When;
import java.lang.annotation.*;

@Documented
@TypeQualifierNickname
@Nonnull(when = When.MAYBE)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LateInitialization {
}
