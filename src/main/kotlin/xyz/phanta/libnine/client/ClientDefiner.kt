package xyz.phanta.libnine.client

import net.minecraft.client.Minecraft
import net.minecraft.particles.IParticleData
import net.minecraft.util.ResourceLocation
import net.minecraft.util.registry.IRegistry
import net.minecraftforge.eventbus.api.IEventBus
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

    fun <X> particle(dest: KMutableProperty0<(X) -> IParticleData>, typeFactory: (ResourceLocation) -> NineParticleType<X>) {
        val name = reg.mod.resource(dest.name.snakeify())
        val type = typeFactory(name)
        IRegistry.field_212632_u.put(name, type.type)
        Minecraft.getInstance().particles.registerFactory(type.type, type.factory)
        dest.set { NineParticleType.Data(type, it) }
    }

}

class ClientRegistrar internal constructor(mod: Virtue, bus: IEventBus, regHandler: RegistryHandler)
    : Registrar(mod, bus, regHandler)
