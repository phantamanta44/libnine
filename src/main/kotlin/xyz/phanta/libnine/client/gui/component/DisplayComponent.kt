package xyz.phanta.libnine.client.gui.component

import xyz.phanta.libnine.util.render.drawTooltip
import xyz.phanta.libnine.util.math.PlanarVec
import xyz.phanta.libnine.util.render.TextureRegion

class ScreenComponentVerticalBar(
        pos: PlanarVec,
        private val bg: TextureRegion,
        private val fg: TextureRegion,
        offsetX: Int,
        offsetY: Int,
        private val dataSrc: () -> Float,
        private val ttSrc: () -> String
) : ScreenComponent(pos, bg.width, bg.height) {

    private val innerPos: PlanarVec = pos.add(offsetX, offsetY)
    private val innerWidth: Int = width - 2 * offsetX
    private val innerHeight: Int = height - 2 * offsetY

    override fun render(partialTicks: Float, mousePos: PlanarVec, mouseOver: Boolean) {
        bg.draw(pos, width, height)
        fg.drawPartial(innerPos, v1 = 1 - dataSrc(), width = innerWidth, height =  innerHeight)
    }

    override fun renderTooltip(partialTicks: Float, mousePos: PlanarVec) = gui.drawTooltip(mousePos, ttSrc())

}

class ScreenComponentTooltip(pos: PlanarVec, private val texture: TextureRegion, private val src: () -> String)
    : ScreenComponent(pos, texture.width, texture.height) {

    override fun render(partialTicks: Float, mousePos: PlanarVec, mouseOver: Boolean) = texture.draw(pos, width, height)

    override fun renderTooltip(partialTicks: Float, mousePos: PlanarVec) = gui.drawTooltip(mousePos, src())

}
