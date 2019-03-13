package xyz.phanta.libnine.definition

import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.IEventBus
import xyz.phanta.libnine.RegistryHandler
import xyz.phanta.libnine.Virtue
import xyz.phanta.libnine.client.ClientDefContext
import xyz.phanta.libnine.client.ClientDefiner
import xyz.phanta.libnine.client.ClientRegistrar

abstract class InitializationContext(val eventBus: IEventBus) {

    abstract fun define(vararg definers: Definer)

    abstract fun defClient(vararg definers: ClientDefiner)

    inline fun <reified E : Event> listen(
            priority: EventPriority = EventPriority.NORMAL,
            receiveCancelled: Boolean = false,
            noinline listener: (E) -> Unit
    ) {
        eventBus.addListener(priority, receiveCancelled, E::class.java, listener)
    }

}

internal class ServerInitContext internal constructor(mod: Virtue, eventBus: IEventBus, regHandler: RegistryHandler)
    : InitializationContext(eventBus) {

    private val registrar: Registrar = Registrar(mod, eventBus, regHandler)

    override fun define(vararg definers: Definer) {
        DefinitionDefContext(registrar).let { ctx -> definers.forEach { it.definitions()(ctx) } }
    }

    override fun defClient(vararg definers: ClientDefiner) {
        // NO-OP
    }

}

internal class CommonInitContext internal constructor(mod: Virtue, eventBus: IEventBus, regHandler: RegistryHandler)
    : InitializationContext(eventBus) {

    private val registrar: ClientRegistrar = ClientRegistrar(mod, eventBus, regHandler)

    override fun define(vararg definers: Definer) {
        DefinitionDefContext(registrar).let { ctx -> definers.forEach { it.definitions()(ctx) } }
    }

    override fun defClient(vararg definers: ClientDefiner) {
        ClientDefContext(registrar).let { ctx -> definers.forEach { it.definitions()(ctx) } }
    }

}
