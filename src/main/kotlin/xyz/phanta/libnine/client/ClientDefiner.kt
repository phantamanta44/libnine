package xyz.phanta.libnine.client

import net.minecraftforge.client.event.ParticleFactoryRegisterEvent
import net.minecraftforge.eventbus.api.IEventBus
import xyz.phanta.libnine.RegistryHandler
import xyz.phanta.libnine.Virtue
import xyz.phanta.libnine.definition.DefBody
import xyz.phanta.libnine.definition.DefDsl
import xyz.phanta.libnine.definition.Registrar

typealias ClientDefinitions = DefBody<ClientDefContext>

interface ClientDefiner {

    fun definitions(): ClientDefinitions

}

@DefDsl
class ClientDefContext(private val registrar: ClientRegistrar) {
    // NO-OP
}

class ClientRegistrar internal constructor(mod: Virtue, bus: IEventBus, regHandler: RegistryHandler)
    : Registrar(mod, bus, regHandler) {

    init {
        bus.addListener<ParticleFactoryRegisterEvent> { particles.forEach { it.registerFactory() } }
    }

}
