package xyz.phanta.libnine.util

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager

fun String.getMcHeight(): Int = Minecraft.getInstance().fontRenderer.FONT_HEIGHT

fun String.getMcWidth(): Int = Minecraft.getInstance().fontRenderer.getStringWidth(this)

fun drawRect(x: Int, y: Int, width: Int, height: Int, colour: Int) {
    Gui.drawRect(x, y, x + width, y + height, colour)
    GlStateManager.color4f(1f, 1f, 1f, 1f)
}
