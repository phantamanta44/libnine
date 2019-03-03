package xyz.phanta.libnine.util

import net.minecraft.client.Minecraft
import net.minecraft.client.audio.SimpleSound
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import xyz.phanta.libnine.util.math.PlanarVec

const val DEF_TEXT_COL = 0x404040

val FONT_HEIGHT: Int
    get() = Minecraft.getInstance().fontRenderer.FONT_HEIGHT

object DrawUtil {

    fun rect(pos: PlanarVec, width: Int, height: Int, colour: Int) {
        Gui.drawRect(pos.x, pos.y, pos.x + width, pos.y + height, colour)
        GlStateManager.color4f(1f, 1f, 1f, 1f)
    }

    fun string(string: String, x: Float, y: Float, colour: Int, shadow: Boolean = false) {
        if (shadow) {
            Minecraft.getInstance().fontRenderer.drawString(string, x, y, colour)
        } else {
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(string, x, y, colour)
        }
    }

}

fun String.getMcWidth(): Int = Minecraft.getInstance().fontRenderer.getStringWidth(this)

fun GuiScreen.drawTooltip(pos: PlanarVec, vararg lines: String) {
    this.drawHoveringText(lines.asList(), pos.x, pos.y)
    RenderHelper.enableGUIStandardItemLighting()
}

fun GuiScreen.drawTooltip(pos: PlanarVec, lines: List<String>) {
    this.drawHoveringText(lines, pos.x, pos.y)
    RenderHelper.enableGUIStandardItemLighting()
}

fun ResourceLocation.bindTexture() = Minecraft.getInstance().textureManager.bindTexture(this)
