package xyz.phanta.libnine.config

import kotlin.reflect.KProperty

interface ConfigProperty<T : Any> {

    operator fun getValue(receiver: Any, prop: KProperty<*>): T

}

internal class ConfigObject<T : Any>(private val value: T) : ConfigProperty<T> {

    override fun getValue(receiver: Any, prop: KProperty<*>): T = value

}

internal class ConfigField<T : Any>(internal var value: T) : ConfigProperty<T> {

    override fun getValue(receiver: Any, prop: KProperty<*>): T = value

}
