package xyz.phanta.libnine.util.render

import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.VertexFormat
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL32

enum class DrawMode(val glConst: Int) {

    POINTS(GL11.GL_POINTS), LINE_STRIP(GL11.GL_LINE_STRIP), LINE_LOOP(GL11.GL_LINE_LOOP), LINES(GL11.GL_LINES),
    TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP), TRIANGLE_FAN(GL11.GL_TRIANGLE_FAN), TRIANGLES(GL11.GL_TRIANGLES),
    QUAD_STRIP(GL11.GL_QUAD_STRIP), QUADS(GL11.GL_QUADS), POLYGON(GL11.GL_POLYGON),
    LINE_STRIP_ADJACENCY(GL32.GL_LINE_STRIP_ADJACENCY), LINES_ADJACENCY(GL32.GL_LINES_ADJACENCY),
    TRIANGLE_STRIP_ADJACENCY(GL32.GL_TRIANGLE_STRIP_ADJACENCY), TRIANGLES_ADJACENCY(GL32.GL_TRIANGLES_ADJACENCY)

}

inline fun tessellate(mode: DrawMode, vertexFormat: VertexFormat, body: BufferBuilder.() -> Unit) {
    val tess = Tessellator.getInstance()
    tess.buffer.let {
        it.begin(mode.glConst, vertexFormat)
        body(it)
    }
    tess.draw()
}

fun BufferBuilder.pos(x: Number, y: Number, z: Number = 0): BufferBuilder = pos(x.toDouble(), y.toDouble(), z.toDouble())
