package xyz.phanta.libnine.config

import net.minecraftforge.common.ForgeConfigSpec

class ConfigBlock {

    private val components: MutableList<ConfigComponent> = mutableListOf()

    fun int(path: String, defValue: Int, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): ConfigProperty<Int> =
            field(defValue) { it.defineInRange(path, defValue, min, max) }

    fun long(path: String, defValue: Long, min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE): ConfigProperty<Long> =
            field(defValue) { it.defineInRange(path, defValue, min, max) }

    fun double(path: String, defValue: Double, min: Double = Double.MIN_VALUE, max: Double = Double.MAX_VALUE): ConfigProperty<Double> =
            field(defValue) { it.defineInRange(path, defValue, min, max) }

    fun bool(path: String, defValue: Boolean): ConfigProperty<Boolean> = field(defValue) { it.define(path, defValue) }

    fun <E : Enum<E>> enum(path: String, defValue: E): ConfigProperty<E> = field(defValue) { it.defineEnum(path, defValue) }

    fun <T : Any> prop(path: String, defValue: T): ConfigProperty<T> = field(defValue) { it.define(path, defValue) }

    fun <T : Any> field(defValue: T, extractFun: (ForgeConfigSpec.Builder) -> ForgeConfigSpec.ConfigValue<T>): ConfigProperty<T> {
        val field = ConfigField(defValue)
        components += ConfigComponent.Field(extractFun, { field.value = it.get() })
        return field
    }

    fun <T : Any> block(path: String, definer: (ConfigBlock) -> T): ConfigProperty<T> {
        val subBlock = ConfigBlock()
        val result = definer(subBlock)
        components += ConfigComponent.SubBlock(path, subBlock)
        return ConfigObject(result)
    }

    internal fun populate(builder: ForgeConfigSpec.Builder) = components.forEach { it.populate(builder) }

    internal fun refresh() = components.forEach { it.refresh() }

}

private interface ConfigComponent {

    fun populate(builder: ForgeConfigSpec.Builder)

    fun refresh() {
        // NO-OP
    }

    class Field<T : Any>(
            private val extractFun: (ForgeConfigSpec.Builder) -> T,
            private val refreshFun: (T) -> Unit
    ) : ConfigComponent {
        private lateinit var field: T

        override fun populate(builder: ForgeConfigSpec.Builder) {
            field = extractFun(builder)
        }

        override fun refresh() {
            refreshFun(field)
        }
    }

    class SubBlock(private val path: String, private val block: ConfigBlock) : ConfigComponent {
        override fun populate(builder: ForgeConfigSpec.Builder) {
            builder.push(path)
            block.populate(builder)
            builder.pop()
        }
    }

}
