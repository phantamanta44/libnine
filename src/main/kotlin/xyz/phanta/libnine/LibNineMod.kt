package xyz.phanta.libnine

import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import xyz.phanta.libnine.definition.InitializationContext

@Mod("libnine")
object Nine : Virtue() {

    val LOGGER: Logger = LogManager.getLogger("libnine")

    override fun init(context: InitializationContext) {
        // NO-OP
    }

}
