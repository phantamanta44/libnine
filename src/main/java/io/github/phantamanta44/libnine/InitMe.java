package io.github.phantamanta44.libnine;

import net.minecraftforge.fml.relauncher.Side;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InitMe {

    String value() default "";

    Side[] sides() default { Side.SERVER, Side.CLIENT };

}
