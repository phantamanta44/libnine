package xyz.phanta.libnine.capability

import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import java.util.*

interface CapabilityBroker {

    fun <T> getCapability(cap: Capability<T>): LazyOptional<T>

}

interface CapabilityStore : CapabilityBroker {

    fun <T> putCapability(cap: Capability<T>, aspect: () -> T)

    @Suppress("USELESS_CAST")
    fun <T> putCapability(cap: Capability<T>, aspect: T) = putCapability(cap, { aspect } as () -> T)

}

open class SimpleCapabilityBroker : CapabilityStore, ICapabilityProvider {

    private val aspects: MutableMap<Capability<*>, LazyOptional<*>> = mutableMapOf()

    override fun <T> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> = getCapability(cap)

    @Suppress("UNCHECKED_CAST")
    override fun <T> getCapability(cap: Capability<T>): LazyOptional<T> =
            (aspects[cap] ?: LazyOptional.empty<T>()) as LazyOptional<T>

    override fun <T> putCapability(cap: Capability<T>, aspect: () -> T) {
        aspects[cap] = LazyOptional.of(aspect)
    }

}

class SidedCapabilityBroker : SimpleCapabilityBroker() {

    private val faces: EnumMap<Direction, CapabilityStore> = EnumMap(Direction::class.java)

    init {
        Direction.values().forEach { faces[it] = SimpleCapabilityBroker() }
    }

    override fun <T> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (side == null) return super.getCapability(cap)
        val sided = faces[side]!!.getCapability(cap)
        return if (sided.isPresent) sided else super.getCapability(cap)
    }

    fun <T> putCapability(side: Direction, cap: Capability<T>, aspect: () -> T) {
        faces[side]!!.putCapability(cap, aspect)
    }

    @Suppress("USELESS_CAST")
    fun <T> putCapability(side: Direction, cap: Capability<T>, aspect: T) =
            putCapability(side, cap, { aspect } as () -> T)

}

class PredicatedCapabilityBroker : ICapabilityProvider {

    private val aspects: MutableMap<Capability<*>, Pair<Function<Boolean>, *>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    override fun <T> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> =
            (aspects[cap] as? Pair<(T, Direction?) -> Boolean, T>)?.let {
                if (it.first(it.second, side)) LazyOptional.of { it.second } else null
            } ?: LazyOptional.empty()

    fun <T> putCapability(cap: Capability<T>, predicate: (T, Direction?) -> Boolean, aspect: T) {
        aspects[cap] = predicate to aspect
    }

}
