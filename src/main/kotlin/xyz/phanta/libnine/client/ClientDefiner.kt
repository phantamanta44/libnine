package xyz.phanta.libnine.client

import net.minecraft.particles.IParticleData
import net.minecraft.util.ResourceLocation
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent
import xyz.phanta.libnine.RegistryHandler
import xyz.phanta.libnine.Virtue
import xyz.phanta.libnine.client.fx.NineParticleType
import xyz.phanta.libnine.definition.DefBody
import xyz.phanta.libnine.definition.Registrar
import xyz.phanta.libnine.util.snakeify
import kotlin.reflect.KMutableProperty0

interface ClientDefiner {

    fun definitions(): DefBody<ClientDefContext>

}

class ClientDefContext(private val reg: ClientRegistrar) {

    fun <X> particleCtx(dest: KMutableProperty0<(X) -> IParticleData>, typeFactory: (ResourceLocation) -> NineParticleType<X>) {
        val type = typeFactory(reg.mod.resource(dest.name.snakeify()))
        reg.particles += type
        dest.set { NineParticleType.Data(type, it) }
    }

    fun particle(dest: KMutableProperty0<() -> IParticleData>, typeFactory: (ResourceLocation) -> NineParticleType<Unit>) {
        val type = typeFactory(reg.mod.resource(dest.name.snakeify()))
        reg.particles += type
        dest.set { NineParticleType.Data(type, Unit) }
    }

}

class ClientRegistrar internal constructor(mod: Virtue, bus: IEventBus, regHandler: RegistryHandler)
    : Registrar(mod, bus, regHandler) {

    internal val particles: MutableList<NineParticleType<*>> = mutableListOf()

    init {
        bus.addListener<FMLLoadCompleteEvent> { particles.forEach { it.register() } }
    }

}
