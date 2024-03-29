package io.github.phantamanta44.libnine.util.render;

import io.github.phantamanta44.libnine.util.format.TextFormatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class FluidRenderUtils {

    @Nullable
    public static TextureAtlasSprite prepareRender(@Nullable FluidStack fluidStack) {
        if (fluidStack == null) {
            return null;
        }
        Fluid fluid = fluidStack.getFluid();
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(fluid.getStill(fluidStack).toString());
        int colour = fluid.getColor(fluidStack);
        TextFormatUtils.setGlColour(colour, TextFormatUtils.getComponent(colour, 3));
        return sprite;
    }

    public static void renderFluidIntoGui(Tessellator tess, BufferBuilder buf, int x, int y, int width, int height,
                                          TextureAtlasSprite sprite, double fraction) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int fluidHeight = Math.round(height * (float)Math.min(1D, Math.max(0D, fraction)));
        double x2 = x + width;
        while (fluidHeight > 0) {
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            double y1 = y + height - fluidHeight, y2 = y1 + Math.min(fluidHeight, width);
            double u1 = sprite.getMinU(), v1 = sprite.getMinV(), u2 = sprite.getMaxU(), v2 = sprite.getMaxV();
            if (fluidHeight < width) {
                v2 = v1 + (v2 - v1) * (fluidHeight / (double)width);
                fluidHeight = 0;
            } else {
                //noinspection SuspiciousNameCombination
                fluidHeight -= width;
            }
            buf.pos(x, y1, 0D).tex(u1, v1).endVertex();
            buf.pos(x, y2, 0D).tex(u1, v2).endVertex();
            buf.pos(x2, y2, 0D).tex(u2, v2).endVertex();
            buf.pos(x2, y1, 0D).tex(u2, v1).endVertex();
            tess.draw();
        }
    }

    public static void renderFluidIntoGui(Tessellator tess, BufferBuilder buf, int x, int y, int width, int height,
                                          @Nullable FluidStack fluidStack, int capacity) {
        if (fluidStack != null && fluidStack.amount > 0) {
            TextureAtlasSprite sprite = FluidRenderUtils.prepareRender(fluidStack);
            if (sprite != null) {
                renderFluidIntoGui(tess, buf, x, y, width, height, sprite,
                        Math.max(fluidStack.amount / (double)capacity, 1D / height)); // ensure at least 1 pixel is drawn
            }
        }
    }

    public static void renderFluidIntoGuiCleanly(int x, int y, int width, int height,
                                                 @Nullable FluidStack fluidStack, int capacity) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Tessellator tess = Tessellator.getInstance();
        renderFluidIntoGui(tess, tess.getBuffer(), x, y, width, height, fluidStack, capacity);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

}
