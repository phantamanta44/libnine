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

@Mod("libnine")
object Nine : Virtue() {

    val LOGGER: Logger = LogManager.getLogger("libnine")

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
