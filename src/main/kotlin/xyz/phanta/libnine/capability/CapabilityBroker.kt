package xyz.phanta.libnine.capability

import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import java.util.*

@Suppress("UNCHECKED_CAST")
internal fun <T> Lazy<*>?.asCapabilityOptional(): LazyOptional<T> =
        this?.let { LazyOptional.of { this.value as T } } ?: LazyOptional.empty()

interface CapabilityBroker {

    fun <T> getCapability(cap: Capability<T>): LazyOptional<T>

}

interface CapabilityStore : CapabilityBroker {

    fun <T> putCapability(cap: Capability<T>, aspect: Lazy<T>)

    fun <T> putCapability(cap: Capability<T>, aspect: T) = putCapability(cap, lazy { aspect })

}

open class SimpleCapabilityBroker : CapabilityStore, ICapabilityProvider {

    private val aspects: MutableMap<Capability<*>, Lazy<*>> = mutableMapOf()

    override fun <T> getCapability(cap: Capability<T>, side: EnumFacing?): LazyOptional<T> = getCapability(cap)

    override fun <T> getCapability(cap: Capability<T>): LazyOptional<T> = aspects[cap].asCapabilityOptional()

    override fun <T> putCapability(cap: Capability<T>, aspect: Lazy<T>) {
        aspects[cap] = aspect
    }

}

class SidedCapabilityBroker : SimpleCapabilityBroker() {

    private val faces: EnumMap<EnumFacing, CapabilityStore> = EnumMap(EnumFacing::class.java)

    init {
        EnumFacing.values().forEach { faces[it] = SimpleCapabilityBroker() }
    }

    override fun <T> getCapability(cap: Capability<T>, side: EnumFacing?): LazyOptional<T> {
        if (side == null) return super.getCapability(cap)
        val sided = faces[side]!!.getCapability(cap)
        return if (sided.isPresent) sided else super.getCapability(cap)
    }

    fun <T> putCapability(side: EnumFacing, cap: Capability<T>, aspect: Lazy<T>) {
        faces[side]!!.putCapability(cap, aspect)
    }

    fun <T> putCapability(side: EnumFacing, cap: Capability<T>, aspect: T) = putCapability(side, cap, lazy { aspect })

}

class PredicatedCapabilityBroker : ICapabilityProvider {

    private val aspects: MutableMap<Capability<*>, Pair<Function<Boolean>, *>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    override fun <T> getCapability(cap: Capability<T>, side: EnumFacing?): LazyOptional<T> =
            (aspects[cap] as? Pair<(T, EnumFacing?) -> Boolean, T>)?.let {
                if (it.first(it.second, side)) LazyOptional.of { it.second } else null
            } ?: LazyOptional.empty()

    fun <T> putCapability(cap: Capability<T>, predicate: (T, EnumFacing?) -> Boolean, aspect: T) {
        aspects[cap] = predicate to aspect
    }

}
