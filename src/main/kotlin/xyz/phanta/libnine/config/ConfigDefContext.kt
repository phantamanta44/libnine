package xyz.phanta.libnine.config

import net.minecraftforge.fml.config.ModConfig
import xyz.phanta.libnine.definition.DefDsl

@DefDsl
interface ConfigDefContext {

    fun config(spec: ConfigBlock, type: ModConfig.Type = ModConfig.Type.COMMON, fileName: String? = null)

}
