package io.github.phantamanta44.libnine.event;

import net.minecraftforge.fml.relauncher.Side;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModDependentEventBusSubscriber {

    Side[] side() default { Side.CLIENT, Side.SERVER };

    String[] dependencies();

}
