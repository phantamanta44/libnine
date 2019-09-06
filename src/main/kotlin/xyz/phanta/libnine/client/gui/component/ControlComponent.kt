package xyz.phanta.libnine.client.gui.component

import net.minecraft.util.SoundEvents
import org.lwjgl.glfw.GLFW
import xyz.phanta.libnine.util.*
import xyz.phanta.libnine.util.math.PlanarVec
import xyz.phanta.libnine.util.render.*
import xyz.phanta.libnine.util.component.redstone.RedstoneBehaviour
import xyz.phanta.libnine.util.world.RedstoneControllable

class ScreenComponentTextInput(
        pos: PlanarVec,
        private val boxLength: Int,
        private val textLength: Int,
        private val btnTex: TextureRegion,
        private val btnTexHover: TextureRegion,
        private val btnTextDisabled: TextureRegion,
        private val validColour: Int,
        private val invalidColour: Int,
        private val validator: (String) -> Boolean,
        private val callback: (String) -> Unit,
        value: String = ""
) : ScreenComponent(pos, boxLength + 9 + FONT_HEIGHT, FONT_HEIGHT + 4) {

    private val buttonOrigin: PlanarVec = pos.add(boxLength + 5, 0)
    private var value: String = value
        set(value) {
            field = value.substring(0, Math.min(value.length, textLength))
            updateValidity()
        }
    private var focused: Boolean = false
    private var valid: Boolean = false

    init {
        updateValidity()
    }

    private fun updateValidity() {
        valid = validator(value)
    }

    private fun isMouseOverButton(mousePos: PlanarVec): Boolean =
            mousePos.inRect(buttonOrigin, FONT_HEIGHT + 4, FONT_HEIGHT + 4)

    override fun render(partialTicks: Float, mousePos: PlanarVec, mouseOver: Boolean) {
        when {
            !valid -> btnTextDisabled
            isMouseOverButton(mousePos) -> btnTexHover
            else -> btnTex
        }.draw(buttonOrigin, 13, 13)
        val colour = if (valid) validColour else invalidColour
        DrawUtil.string(value, pos.x + 2F, pos.y + 3F, colour)
        if (focused && System.currentTimeMillis() % 1000 < 500) {
            DrawUtil.rect(pos.add(value.getMcWidth() + 2, 2), 1, FONT_HEIGHT, colour or -0x1000000) // 0xFF000000
        }
    }

    override fun onMouseDown(mousePos: PlanarVec, button: Int, mouseOver: Boolean): Boolean {
        if (mouseOver) {
            if (mousePos.inRect(pos, boxLength + 4, FONT_HEIGHT + 4)) {
                if (button == 1) value = ""
                focused = true
            } else {
                focused = false
                if (isMouseOverButton(mousePos) && valid) {
                    SoundEvents.UI_BUTTON_CLICK.play()
                    callback(value)
                }
            }
            return true
        } else {
            focused = false
            return false
        }
    }

    override fun onKeyDown(keyCode: Int, mods: Int): Boolean {
        if (focused) {
            val currentLength = value.length
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !value.isEmpty()) {
                if (mods and GLFW.GLFW_MOD_CONTROL != 0) {
                    var endIndex = 0
                    if (Character.isLetterOrDigit(value[currentLength - 1])) {
                        for (i in currentLength - 2 downTo 0) {
                            if (!Character.isLetterOrDigit(value[i])) {
                                endIndex = i
                                break
                            }
                        }
                    } else {
                        for (i in currentLength - 2 downTo 0) {
                            if (Character.isLetterOrDigit(value[i])) {
                                endIndex = i
                                break
                            }
                        }
                    }
                    value = value.substring(0, endIndex + 1)
                } else {
                    value = value.substring(0, currentLength - 1)
                }
                updateValidity()
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                updateValidity()
                if (valid) callback(value)
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                focused = false
            } else {
                return false
            }
            return true
        }
        return false
    }

    override fun onKeyType(keyCode: Int, typed: Char): Boolean {
        if (focused && value.length < textLength && typed.toInt() >= 32 && typed.toInt() < 127) {
            value += typed
            updateValidity()
            return true
        }
        return false
    }

    override fun onFocusChange(newState: Boolean): Boolean {
        focused = newState
        return true
    }

}

class ScreenComponentRedstoneControl(
        pos: PlanarVec,
        private val bg: TextureRegion,
        private val bgHover: TextureRegion,
        private val ignored: TextureRegion,
        private val direct: TextureRegion,
        private val inverted: TextureRegion,
        offsetX: Int,
        offsetY: Int,
        private val descIgnored: String,
        private val descDirect: String,
        private val descInverted: String,
        private val target: RedstoneControllable
) : ScreenComponent(pos, bg.width, bg.height) {

    private val innerPos: PlanarVec = pos.add(offsetX, offsetY)

    override fun render(partialTicks: Float, mousePos: PlanarVec, mouseOver: Boolean) {
        (if (mouseOver) bgHover else bg).draw(pos, width, height)
        when (target.redstoneBehaviour) {
            RedstoneBehaviour.IGNORED -> ignored.draw(innerPos, width, height)
            RedstoneBehaviour.DIRECT -> direct.draw(innerPos, width, height)
            RedstoneBehaviour.INVERTED -> inverted.draw(innerPos, width, height)
        }
    }

    override fun renderTooltip(partialTicks: Float, mousePos: PlanarVec) {
        when (target.redstoneBehaviour) {
            RedstoneBehaviour.IGNORED -> gui.drawTooltip(mousePos, descIgnored)
            RedstoneBehaviour.DIRECT -> gui.drawTooltip(mousePos, descDirect)
            RedstoneBehaviour.INVERTED -> gui.drawTooltip(mousePos, descInverted)
        }
    }

    override fun onMouseDown(mousePos: PlanarVec, button: Int, mouseOver: Boolean): Boolean {
        if (mouseOver) {
            when (button) {
                GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
                    target.redstoneBehaviour = target.redstoneBehaviour.next()
                }
                GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
                    target.redstoneBehaviour = target.redstoneBehaviour.prev()
                }
                else -> return false
            }
            SoundEvents.UI_BUTTON_CLICK.play()
            return true
        }
        return false
    }

}
