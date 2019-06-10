package xyz.phanta.libnine.client

import net.minecraft.particles.ParticleType
import net.minecraftforge.eventbus.api.IEventBus
import xyz.phanta.libnine.RegistryHandler
import xyz.phanta.libnine.Virtue
import xyz.phanta.libnine.definition.DefBody
import xyz.phanta.libnine.definition.Registrar

interface ClientDefiner {

    fun definitions(): DefBody<ClientDefContext>

}

class ClientDefContext(private val registrar: ClientRegistrar) {
    // NO-OP
}

class ClientRegistrar internal constructor(mod: Virtue, bus: IEventBus, regHandler: RegistryHandler)
    : Registrar(mod, bus, regHandler) {

    init {
        regHandler.registerProvider<ParticleType<*>> { particles.forEach { it.registerFactory() } }
    }

}
