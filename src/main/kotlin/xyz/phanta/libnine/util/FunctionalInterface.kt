package xyz.phanta.libnine.util

import net.minecraftforge.common.util.LazyOptional
import java.util.function.Consumer

infix fun <T> Consumer<T>.then(other: Consumer<T>): Consumer<T> = this.andThen(other)

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun <T> LazyOptional<T>.orNull(): T? = this.orElse(null)
