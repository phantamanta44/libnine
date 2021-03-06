package xyz.phanta.libnine.client.gui.component

import net.minecraft.client.gui.IGuiEventListener
import net.minecraft.client.gui.screen.Screen
import xyz.phanta.libnine.util.math.MutablePlanarVec
import xyz.phanta.libnine.util.math.PlanarVec

abstract class ScreenComponent(val pos: PlanarVec, val width: Int, val height: Int) {

    lateinit var gui: Screen
        internal set

    abstract fun render(partialTicks: Float, mousePos: PlanarVec, mouseOver: Boolean)

    open fun renderTooltip(partialTicks: Float, mousePos: PlanarVec) {
        // NO-OP
    }

    open fun onMouseDown(mousePos: PlanarVec, button: Int, mouseOver: Boolean): Boolean = false

    open fun onMouseUp(mousePos: PlanarVec, button: Int, mouseOver: Boolean): Boolean = false

    open fun onMouseScroll(mousePos: PlanarVec, amount: Double): Boolean = false

    open fun onMouseDrag(mousePos: PlanarVec, button: Int, diffX: Double, diffY: Double, mouseOver: Boolean): Boolean = false

    open fun onKeyDown(keyCode: Int, mods: Int): Boolean = false

    open fun onKeyUp(keyCode: Int, mods: Int): Boolean = false

    open fun onKeyType(keyCode: Int, typed: Char): Boolean = false

    open fun onFocusChange(newState: Boolean): Boolean = false

}

class GuiComponentManager(private val gui: Screen) : IGuiEventListener {

    private val comps: MutableList<ComponentState> = mutableListOf()

    @Suppress("UNCHECKED_CAST")
    fun register(comp: ScreenComponent) {
        comp.gui = gui
        ComponentState(comp).let {
            comps += it
            (gui.children() as MutableList<IGuiEventListener>).add(it)
        }
    }

    fun draw(partialTicks: Float, mousePos: PlanarVec) {
        comps.forEach { it.drawAndUpdate(partialTicks, mousePos) }
        comps.forEach { it.drawTooltip(partialTicks, mousePos) }
    }

    private class ComponentState internal constructor(private val comp: ScreenComponent) : IGuiEventListener {

        private val cachedMousePos: MutablePlanarVec = MutablePlanarVec(0, 0)
        private var mouseOver: Boolean = false

        internal fun drawAndUpdate(partialTicks: Float, mousePos: PlanarVec) {
            cachedMousePos.assignFrom(mousePos)
            mouseOver = mousePos.inRect(comp.pos, comp.width, comp.height)
            comp.render(partialTicks, mousePos, mouseOver)
        }

        internal fun drawTooltip(partialTicks: Float, mousePos: PlanarVec) {
            if (mouseOver) comp.renderTooltip(partialTicks, mousePos)
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean =
                comp.onMouseDown(cachedMousePos.assignFrom(mouseX.toInt(), mouseY.toInt()), button, mouseOver)

        override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean =
                comp.onMouseUp(cachedMousePos.assignFrom(mouseX.toInt(), mouseY.toInt()), button, mouseOver)

        override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean =
                comp.onMouseScroll(cachedMousePos.assignFrom(mouseX.toInt(), mouseY.toInt()), amount)

        override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, diffX: Double, diffY: Double): Boolean =
                comp.onMouseDrag(cachedMousePos.assignFrom(mouseX.toInt(), mouseY.toInt()), button, diffX, diffY, mouseOver)

        override fun keyPressed(keyCode: Int, scanCode: Int, mods: Int): Boolean =
                comp.onKeyDown(keyCode, mods)

        override fun keyReleased(keyCode: Int, scanCode: Int, mods: Int): Boolean =
                comp.onKeyUp(keyCode, mods)

        override fun charTyped(typed: Char, keyCode: Int): Boolean = comp.onKeyType(keyCode, typed)

        override fun changeFocus(focused: Boolean): Boolean = comp.onFocusChange(focused)

        override fun isMouseOver(p_isMouseOver_1_: Double, p_isMouseOver_3_: Double): Boolean = mouseOver

    }

}
