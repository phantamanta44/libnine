package xyz.phanta.libnine

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.event.TickEvent
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import xyz.phanta.libnine.client.event.ClientTickHandler
import xyz.phanta.libnine.definition.InitializationContext
import xyz.phanta.libnine.tile.NineTile

@Mod(Nine.MOD_ID)
object Nine : Virtue() {

    const val MOD_ID: String = "libnine"

    val LOGGER: Logger = LogManager.getLogger(MOD_ID)

    override fun init(context: InitializationContext) {
        DistExecutor.runWhenOn(Dist.CLIENT) { Runnable { NineClient.clientInit(context) } }
        context.listen<TickEvent.ServerTickEvent> { NineTile.flushDirtySet() }
    }

}

object NineClient {

    val ticker: ClientTickHandler = ClientTickHandler()

    fun clientInit(context: InitializationContext) {
        context.eventBus.addListener<TickEvent.ClientTickEvent>(ticker)
    }

}
