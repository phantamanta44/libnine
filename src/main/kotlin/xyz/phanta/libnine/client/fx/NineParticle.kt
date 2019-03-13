package xyz.phanta.libnine.client.fx

import net.minecraft.client.Minecraft
import net.minecraft.client.particle.Particle
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World

abstract class NineParticle : Particle {

    constructor(world: World, x: Double, y: Double, z: Double, velX: Double, velY: Double, velZ: Double)
            : super(world, x, y, z, velX, velY, velZ)

    constructor(world: World, x: Double, y: Double, z: Double) : super(world, x, y, z)

    protected fun fixPosition(partialTicks: Float, player: EntityPlayer = Minecraft.getInstance().player) {
        GlStateManager.translatef(
                (prevPosX + (posX - prevPosX) * partialTicks.toDouble()
                        - player.lastTickPosX
                        - (player.posX - player.lastTickPosX) * partialTicks.toDouble()).toFloat(),
                (prevPosY + (posY - prevPosY) * partialTicks.toDouble()
                        - player.lastTickPosY
                        - (player.posY - player.lastTickPosY) * partialTicks.toDouble()).toFloat(),
                (prevPosZ + (posZ - prevPosZ) * partialTicks.toDouble()
                        - player.lastTickPosZ
                        - (player.posZ - player.lastTickPosZ) * partialTicks.toDouble()).toFloat()
        )
    }

}
