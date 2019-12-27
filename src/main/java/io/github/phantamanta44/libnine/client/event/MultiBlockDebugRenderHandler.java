package io.github.phantamanta44.libnine.client.event;

import io.github.phantamanta44.libnine.component.multiblock.IMultiBlockUnit;
import io.github.phantamanta44.libnine.util.render.RenderUtils;
import io.github.phantamanta44.libnine.util.world.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

// adapted from WYNNER barrier sight module
// https://github.com/phantamanta44/WYNNER/blob/1.12/src/main/java/xyz/phanta/wynner/module/barriersight/BarrierRenderer.java
public class MultiBlockDebugRenderHandler {

    private static final int RADIUS = 7;
    private static final int RADIUS_SQ = RADIUS * RADIUS;

    private static boolean enabled = false;

    public static void toggle() {
        enabled = !enabled;
    }

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (!enabled) {
            return;
        }
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.color(1F, 0F, 0F, 1F);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        Vec3d plPos = RenderUtils.getInterpPos(mc.player, event.getPartialTicks());
        BlockPos plCoords = mc.player.getPosition();
        double offX = plPos.x - plCoords.getX(), offY = plPos.y - plCoords.getY(), offZ = plPos.z - plCoords.getZ();
        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -RADIUS; y <= RADIUS + 1; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    BlockPos blCoords = plCoords.add(x, y, z);
                    double distSq = WorldUtils.getBlockCenter(blCoords).squareDistanceTo(plPos.x, plPos.y, plPos.z);
                    if (distSq < RADIUS_SQ) {
                        TileEntity tile = mc.world.getTileEntity(blCoords);
                        if (tile instanceof IMultiBlockUnit) {
                            for (EnumFacing dir : ((IMultiBlockUnit<?>)tile).getMultiBlockConnection().getEmittingDirs()) {
                                double blkX = x - offX + 0.5D;
                                double blkY = y - offY + 0.5D;
                                double blkZ = z - offZ + 0.5D;
                                buf.pos(blkX, blkY, blkZ).endVertex();
                                buf.pos(blkX + dir.getXOffset(), blkY + dir.getYOffset(), blkZ + dir.getZOffset()).endVertex();
                            }
                        }
                    }
                }
            }
        }
        tess.draw();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
    }

}
