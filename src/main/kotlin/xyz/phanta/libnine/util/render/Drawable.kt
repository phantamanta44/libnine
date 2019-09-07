package xyz.phanta.libnine.util.render

import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import xyz.phanta.libnine.util.math.PlanarVec

interface ScreenDrawable {

    val width: Int

    val height: Int

    fun getRegion(x: Int, y: Int, width: Int, height: Int): ScreenDrawable

    fun draw(
            x: Double,
            y: Double,
            width: Double = this.width.toDouble(),
            height: Double = this.height.toDouble(),
            zIndex: Double = 0.0
    )

    fun draw(
            x: Int,
            y: Int,
            width: Int = this.width,
            height: Int = this.height,
            zIndex: Double = 0.0
    ) = draw(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), zIndex)

    fun draw(
            pos: PlanarVec,
            width: Int = this.width,
            height: Int = this.height,
            zIndex: Double = 0.0
    ) = draw(pos.x, pos.y, width, height, zIndex)

    fun drawPartial(
            x: Double,
            y: Double,
            u1: Float = 0F,
            v1: Float = 0F,
            u2: Float = 1F,
            v2: Float = 1F,
            width: Double = this.width.toDouble(),
            height: Double = this.height.toDouble(),
            zIndex: Double = 0.0
    )

    fun drawPartial(
            x: Int,
            y: Int,
            u1: Float = 0F,
            v1: Float = 0F,
            u2: Float = 1F,
            v2: Float = 1F,
            width: Int = this.width,
            height: Int = this.height,
            zIndex: Double = 0.0
    ) = drawPartial(x.toDouble(), y.toDouble(), u1, v1, u2, v2, width.toDouble(), height.toDouble(), zIndex)

    fun drawPartial(
            pos: PlanarVec,
            u1: Float = 0F,
            v1: Float = 0F,
            u2: Float = 1F,
            v2: Float = 1F,
            width: Int = this.width,
            height: Int = this.height,
            zIndex: Double = 0.0
    ) = drawPartial(pos.x, pos.y, u1, v1, u2, v2, width, height, zIndex)

}

class TextureResource(private val texture: ResourceLocation, override val width: Int, override val height: Int) : ScreenDrawable {

    private val fullRegion: TextureRegion by lazy { TextureRegion(this) }

    fun bind() = texture.bindTexture()

    override fun getRegion(x: Int, y: Int, width: Int, height: Int): TextureRegion = TextureRegion(this, x, y, width, height)

    override fun draw(x: Double, y: Double, width: Double, height: Double, zIndex: Double) =
            fullRegion.draw(x, y, width, height, zIndex)

    override fun drawPartial(
            x: Double,
            y: Double,
            u1: Float,
            v1: Float,
            u2: Float,
            v2: Float,
            width: Double,
            height: Double,
            zIndex: Double
    ) = fullRegion.drawPartial(x, y, u1, v1, u2, v2, width, height, zIndex)

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

    override fun draw(x: Double, y: Double, width: Double, height: Double, zIndex: Double) {
        texture.bind()
        tessellate(DrawMode.QUADS, DefaultVertexFormats.POSITION_TEX) {
            pos(x, y + height, zIndex).tex(u1, v2).endVertex()
            pos(x + width, y + height, zIndex).tex(u2, v2).endVertex()
            pos(x + width, y, zIndex).tex(u2, v1).endVertex()
            pos(x, y, zIndex).tex(u1, v1).endVertex()
        }
    }

    override fun drawPartial(
            x: Double,
            y: Double,
            u1: Float,
            v1: Float,
            u2: Float,
            v2: Float,
            width: Double,
            height: Double,
            zIndex: Double
    ) {
        val xStart = x + width * u1 // there's some code that kotlin just can't make better
        val xEnd = x + width * u2
        val yStart = y + height * v1
        val yEnd = y + height * v2
        val uStart = this.u1 + du * u1
        val uEnd = this.u1 + du * u2
        val vStart = this.v1 + dv * v1
        val vEnd = this.v1 + dv * v2
        texture.bind()
        tessellate(DrawMode.QUADS, DefaultVertexFormats.POSITION_TEX) {
            pos(xStart, yEnd, zIndex).tex(uStart, vEnd).endVertex()
            pos(xEnd, yEnd, zIndex).tex(uEnd, vEnd).endVertex()
            pos(xEnd, yStart, zIndex).tex(uEnd, vStart).endVertex()
            pos(xStart, yStart, zIndex).tex(uStart, vStart).endVertex()
        }
    }

}
