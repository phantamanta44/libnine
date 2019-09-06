package xyz.phanta.libnine.util.render

import com.mojang.blaze3d.platform.GLX
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraftforge.fml.ModList
import xyz.phanta.libnine.Nine

private val optifined: Boolean by lazy {
    if (ModList.get().isLoaded("optifine")) {
        Nine.LOGGER.warn("Whoops! You have Optifine installed. That might cause some weird rendering issues.")
        true
    } else {
        false
    }
}

object Lightmap {

    private var cached = false
    private var cachedX: Float = 0.toFloat()
    private var cachedY: Float = 0.toFloat()

    fun setCoords(x: Float, y: Float) {
        if (optifined) return
        if (!cached) cacheCoords(GLX.lastBrightnessX, GLX.lastBrightnessY)
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, x, y)
    }

    private fun cacheCoords(x: Float, y: Float) {
        cachedX = x
        cachedY = y
        cached = true
    }

    fun restore() {
        if (optifined) return
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, cachedX, cachedY)
        cached = false
    }

    fun fullBright() {
        setCoords(240f, 240f)
    }

    inline fun withCoords(x: Float, y: Float, body: () -> Unit) {
        setCoords(x, y)
        body()
        restore()
    }

    inline fun withFullBrightness(body: () -> Unit) {
        fullBright()
        body()
        restore()
    }

}
