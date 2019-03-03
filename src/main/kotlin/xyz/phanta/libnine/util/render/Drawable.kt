package xyz.phanta.libnine.util.render

import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import xyz.phanta.libnine.util.bindTexture

interface ScreenDrawable {

    val width: Int

    val height: Int

    fun getRegion(x: Int, y: Int, width: Int, height: Int): ScreenDrawable

    fun draw(x: Int, y: Int, width: Int, height: Int)

    fun draw(x: Int, y: Int) = draw(x, y, width, height)

    fun drawPartial(x: Int, y: Int, width: Int, height: Int, x1: Float, y1: Float, x2: Float, y2: Float)

    fun drawPartial(x: Int, y: Int, x1: Float, y1: Float, x2: Float, y2: Float) = drawPartial(x, y, width, height, x1, y1, x2, y2)

}

class TextureResource(private val texture: ResourceLocation, override val width: Int, override val height: Int) : ScreenDrawable {

    private val fullRegion: TextureRegion by lazy { TextureRegion(this) }

    fun bind() = texture.bindTexture()

    override fun getRegion(x: Int, y: Int, width: Int, height: Int): TextureRegion = TextureRegion(this, x, y, width, height)

    override fun draw(x: Int, y: Int, width: Int, height: Int) = fullRegion.draw(x, y, width, height)

    override fun drawPartial(x: Int, y: Int, width: Int, height: Int, x1: Float, y1: Float, x2: Float, y2: Float) =
            fullRegion.drawPartial(x, y, width, height, x1, y1, x2, y2)

}

class TextureRegion(
        private val texture: TextureResource,
        private val x: Int,
        private val y: Int,
        override val width: Int,
        override val height: Int
) : ScreenDrawable {

    private val u1: Double = x.toDouble() / texture.width
    private val v1: Double = y.toDouble() / texture.height
    private val u2: Double = u1 + width.toDouble() / texture.width
    private val v2: Double = v1 + height.toDouble() / texture.height
    private val du: Double = u2 - u1
    private val dv: Double = v2 - v1

    constructor(texture: TextureResource) : this(texture, 0, 0, texture.width, texture.height)

    override fun getRegion(x: Int, y: Int, width: Int, height: Int): ScreenDrawable =
            TextureRegion(texture, this.x + x, this.y + y, width, height)

    override fun draw(x: Int, y: Int, width: Int, height: Int) {
        texture.bind()
        tessellate(DrawMode.QUADS, DefaultVertexFormats.POSITION_TEX) {
            pos(x, y + height).tex(u1, v2).endVertex()
            pos(x + width, y + height).tex(u2, v2).endVertex()
            pos(x + width, y).tex(u2, v1).endVertex()
            pos(x, y).tex(u1, v1).endVertex()
        }
    }

    override fun drawPartial(x: Int, y: Int, width: Int, height: Int, x1: Float, y1: Float, x2: Float, y2: Float) {
        val xStart = x + width * x1 // there's some code that kotlin just can't make better
        val xEnd = x + width * x2
        val yStart = y + height * y1
        val yEnd = y + height * y2
        val uStart = u1 + du * x1
        val uEnd = u1 + du * x2
        val vStart = v1 + dv * y1
        val vEnd = v1 + dv * y2
        texture.bind()
        tessellate(DrawMode.QUADS, DefaultVertexFormats.POSITION_TEX) {
            pos(xStart, yEnd).tex(uStart, vEnd).endVertex()
            pos(xEnd, yEnd).tex(uEnd, vEnd).endVertex()
            pos(xEnd, yStart).tex(uEnd, vStart).endVertex()
            pos(xStart, yStart).tex(uStart, vStart).endVertex()
        }
    }

}
