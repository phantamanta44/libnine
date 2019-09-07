package xyz.phanta.libnine.util.render

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.MainWindow
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.MathHelper
import xyz.phanta.libnine.util.math.*


object Renders {

    fun renderWorldOrtho(
            x: Double, y: Double, z: Double,
            scaleX: Float = 1F, scaleY: Float = 1F, angle: Float = 0F,
            u1: Double = 0.0, v1: Double = 0.0, u2: Double = 1.0, v2: Double = 1.0,
            pitch: Float = Minecraft.getInstance().gameRenderer.activeRenderInfo.pitch,
            yaw: Float = Minecraft.getInstance().gameRenderer.activeRenderInfo.yaw
    ) {
        val pitchRad = pitch.degToRad()
        val yawRad = yaw.degToRad()
        val lookX = MathHelper.cos(yawRad)
        val lookZ = MathHelper.sin(yawRad)
        val lookYZ = -lookZ * MathHelper.sin(pitchRad)
        val lookXY = lookX * MathHelper.sin(pitchRad)
        val lookXZ = MathHelper.cos(pitchRad)
        val vertices = arrayOf(
                vec3d(-lookX * scaleX - lookYZ * scaleY, -lookXZ * scaleY, -lookZ * scaleX - lookXY * scaleY),
                vec3d(-lookX * scaleX + lookYZ * scaleY, lookXZ * scaleY, -lookZ * scaleX + lookXY * scaleY),
                vec3d(lookX * scaleX + lookYZ * scaleY, lookXZ * scaleY, lookZ * scaleX + lookXY * scaleY),
                vec3d(lookX * scaleX - lookYZ * scaleY, -lookXZ * scaleY, lookZ * scaleX - lookXY * scaleY)
        )
        val angX = MathHelper.cos(angle / 2F)
        val axial = Minecraft.getInstance().player.lookVec * MathHelper.sin(angle / 2F).toDouble()
        for (i in 0..3) {
            vertices[i] = axial * (2.0 * (vertices[i] dot axial)) +
                    vertices[i] * (angX * angX - axial.lengthSquared()) +
                    (axial cross vertices[i]) * (2.0 * angX)
        }
        tessellate(DrawMode.QUADS, DefaultVertexFormats.POSITION_TEX) {
            pos(x + vertices[0].x, y + vertices[0].y, z + vertices[0].z).tex(u2, v2).endVertex()
            pos(x + vertices[1].x, y + vertices[1].y, z + vertices[1].z).tex(u2, v1).endVertex()
            pos(x + vertices[2].x, y + vertices[2].y, z + vertices[2].z).tex(u1, v1).endVertex()
            pos(x + vertices[3].x, y + vertices[3].y, z + vertices[3].z).tex(u1, v2).endVertex()
        }
    }

    fun renderWorldOrtho(x: Double, y: Double, z: Double,
                         scaleX: Float, scaleY: Float, angle: Float, icon: TextureRegion) {
        icon.texture.bind()
        renderWorldOrtho(x, y, z, scaleX, scaleY, angle, icon.u1, icon.v1, icon.u2, icon.v2)
    }

    fun renderScreenOverlay(texture: ScreenDrawable, window: MainWindow) {
        GlStateManager.enableBlend()
        GlStateManager.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
        GlStateManager.disableAlphaTest()
        GlStateManager.disableDepthTest()
        GlStateManager.depthMask(false)
        texture.draw(0, 0, window.scaledWidth, window.scaledHeight)
        GlStateManager.depthMask(true)
        GlStateManager.enableDepthTest()
        GlStateManager.enableAlphaTest()
    }

}
