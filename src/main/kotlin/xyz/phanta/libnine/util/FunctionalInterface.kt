package xyz.phanta.libnine.util

import java.util.function.Consumer

infix fun <T> Consumer<T>.then(other: Consumer<T>): Consumer<T> = this.andThen(other)
