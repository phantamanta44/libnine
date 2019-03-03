package xyz.phanta.libnine.definition

import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.IEventBus
import xyz.phanta.libnine.Virtue

class InitializationContext(mod: Virtue, val eventBus: IEventBus) {

    private val registrar: Registrar = Registrar(mod, eventBus)

    fun define(vararg definers: Definer) {
        DefinitionDefContext(registrar).let { ctx -> definers.forEach { it.definitions()(ctx) } }
    }

    inline fun <reified E : Event> listen(
            priority: EventPriority = EventPriority.NORMAL,
            receiveCancelled: Boolean = false,
            noinline listener: (E) -> Unit) {
        eventBus.addListener(priority, receiveCancelled, E::class.java, listener)
    }

}
