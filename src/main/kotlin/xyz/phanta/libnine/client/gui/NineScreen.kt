package xyz.phanta.libnine.client.gui

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextComponent
import xyz.phanta.libnine.client.gui.component.GuiComponentManager
import xyz.phanta.libnine.client.gui.component.ScreenComponent
import xyz.phanta.libnine.container.NineContainer
import xyz.phanta.libnine.util.math.MutablePlanarVec
import xyz.phanta.libnine.util.math.PlanarVec
import xyz.phanta.libnine.util.render.DEF_TEXT_COL
import xyz.phanta.libnine.util.render.GlStateUtil
import xyz.phanta.libnine.util.render.bindTexture

interface ComponentScreen {

    fun addComponent(comp: ScreenComponent)

    fun drawBackground(partialTicks: Float, mousePos: PlanarVec)

    fun drawForeground(partialTicks: Float, mousePos: PlanarVec) {
        // NO-OP
    }

}

abstract class NineScreen(
        title: ITextComponent,
        private val bg: ResourceLocation? = null,
        private val sizeX: Int = 176,
        private val sizeY: Int = 166
) : Screen(title), ComponentScreen {

    @Suppress("LeakingThis")
    private val components: GuiComponentManager = GuiComponentManager(this)

    private val pos: MutablePlanarVec = MutablePlanarVec(0, 0)
    private val cachedMousePos: MutablePlanarVec = MutablePlanarVec(0, 0)

    override fun init() {
        super.init()
        pos.x = (this.width - this.sizeX) / 2
        pos.y = (this.height - this.sizeY) / 2
    }

    override fun addComponent(comp: ScreenComponent) = components.register(comp)

    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(partialTicks, cachedMousePos.assignFrom(mouseX - pos.x, mouseY - pos.y))

        GlStateManager.disableRescaleNormal()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableLighting()
        GlStateManager.disableDepthTest()

        super.render(mouseX, mouseY, partialTicks)

        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.pushMatrix()
        GlStateManager.translatef(pos.x.toFloat(), pos.y.toFloat(), 0F)
        GlStateManager.enableRescaleNormal()
        RenderHelper.disableStandardItemLighting()

        drawForeground(partialTicks, cachedMousePos)
        components.draw(partialTicks, cachedMousePos)

        GlStateManager.popMatrix()
        GlStateManager.enableLighting()
        GlStateManager.enableDepthTest()
        RenderHelper.enableStandardItemLighting()
    }

    override fun drawBackground(partialTicks: Float, mousePos: PlanarVec) {
        renderBackground()
        if (bg != null) {
            GlStateUtil.resetColour()
            bg.bindTexture()
            blit(pos.x, pos.y, 0, 0, sizeX, sizeY)
        }
    }

}

abstract class NineScreenContainer<C : NineContainer>(
        container: C,
        playerInv: PlayerInventory,
        title: ITextComponent,
        private val bg: ResourceLocation? = null,
        private val sizeX: Int = 176,
        private val sizeY: Int = 166
) : ContainerScreen<C>(container, playerInv, title), ComponentScreen {

    @Suppress("LeakingThis")
    private val components: GuiComponentManager = GuiComponentManager(this)

    private val pos: MutablePlanarVec = MutablePlanarVec(0, 0)
    private val cachedMousePos: MutablePlanarVec = MutablePlanarVec(0, 0)
    private var partialTicks: Float = 0F

    override fun init() {
        super.init()
        pos.assignFrom((this.width - this.sizeX) / 2, (this.height - this.sizeY) / 2)
    }

    override fun addComponent(comp: ScreenComponent) = components.register(comp)

    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.render(mouseX, mouseY, partialTicks)
        renderHoveredToolTip(mouseX, mouseY)
    }

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseY: Int, mouseX: Int) {
        this.partialTicks = partialTicks
        drawBackground(partialTicks, cachedMousePos.assignFrom(mouseY - pos.x, mouseX - pos.y))
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        drawForeground(partialTicks, cachedMousePos.assignFrom(mouseY - pos.x, mouseX - pos.y))
        components.draw(partialTicks, cachedMousePos)
    }

    override fun drawBackground(partialTicks: Float, mousePos: PlanarVec) {
        renderBackground()
        if (bg != null) {
            GlStateUtil.resetColour()
            bg.bindTexture()
            blit(pos.x, pos.y, 0, 0, sizeX, sizeY)
        }
    }

    override fun drawForeground(partialTicks: Float, mousePos: PlanarVec) = drawPlayerInventoryName()

    protected fun drawContainerName(name: String) {
        font.drawString(name, 8F, 6F, DEF_TEXT_COL)
        GlStateUtil.resetColour()
    }

    protected fun drawPlayerInventoryName() {
        font.drawString(I18n.format("container.inventory"), 8f, (this.ySize - 96 + 2).toFloat(), DEF_TEXT_COL)
        GlStateUtil.resetColour()
    }

}
