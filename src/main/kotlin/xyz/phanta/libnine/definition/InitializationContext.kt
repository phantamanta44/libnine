package xyz.phanta.libnine.definition

import net.minecraftforge.eventbus.api.IEventBus
import xyz.phanta.libnine.Virtue

class InitializationContext(mod: Virtue, eventBus: IEventBus) {

    private val registrar: Registrar = Registrar(mod, eventBus)

    fun define(vararg definers: Definer) {
        DefinitionDefContext(registrar).let { ctx -> definers.forEach { it.definitions()(ctx) } }
    }

}
