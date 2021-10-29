package io.github.phantamanta44.libnine.util.helper;

import io.github.phantamanta44.libnine.item.L9ItemSubs;
import io.github.phantamanta44.libnine.util.IDisplayableMatcher;
import io.github.phantamanta44.libnine.util.world.WorldBlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.util.Iterator;
import java.util.List;

public class ItemUtils {

    public static final short WILDCARD_META = (short)32767;

    public static boolean matchesWithWildcard(ItemStack a, ItemStack b) {
        //noinspection ConstantConditions
        return a.getItem().equals(b.getItem())
                && (a.getMetadata() == WILDCARD_META || b.getMetadata() == WILDCARD_META || a.getMetadata() == b.getMetadata())
                && (a.hasTagCompound() ? (b.hasTagCompound() && b.getTagCompound().equals(a.getTagCompound())) : !b.hasTagCompound());
    }

    public static IDisplayableMatcher<ItemStack> matchesWithWildcard(ItemStack stack) {
        return IDisplayableMatcher.of(() -> stack.getMetadata() == WILDCARD_META
                        ? new ItemStack(stack.getItem(), stack.getCount(), 0) : stack,
                s -> matchesWithWildcard(stack, s));
    }

    public static String getColouredName(ItemStack stack) {
        return stack.getItem().getForgeRarity(stack).getColor() + stack.getDisplayName();
    }

    public static String getUnlocalizedBlockName(WorldBlockPos pos) {
        ItemStack stack = pos.getBlock().getPickBlock(pos.getBlockState(), null, pos.getWorld(), pos.getPos(), null);
        return stack.getTranslationKey();
    }

    public static String getLocalizedBlockName(WorldBlockPos pos) {
        ItemStack stack = pos.getBlock().getPickBlock(pos.getBlockState(), null, pos.getWorld(), pos.getPos(), null);
        return stack.getItem().getItemStackDisplayName(stack);
    }

    public static String getColouredBlockName(WorldBlockPos pos) {
        return getColouredName(
                pos.getBlock().getPickBlock(pos.getBlockState(), null, pos.getWorld(), pos.getPos(), null));
    }

    @SuppressWarnings("deprecation")
    public static String getLocalizedSubName(L9ItemSubs item, int meta) {
        return I18n.translateToLocal(String.format("item.%s%d.name", item.getRegistryName(), meta));
    }

    public static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            stack.setTagCompound(tag = new NBTTagCompound());
        }
        return tag;
    }

    public static void getStackTooltip(ItemStack stack, List<String> tooltip, ITooltipFlag tooltipFlags) {
        List<String> itemTooltip = stack.getTooltip(Minecraft.getMinecraft().player, tooltipFlags);
        if (itemTooltip.isEmpty()) {
            tooltip.add(stack.getItem().getForgeRarity(stack).getColor() + stack.getDisplayName());
            return;
        }
        Iterator<String> iter = itemTooltip.iterator();
        tooltip.add(stack.getItem().getForgeRarity(stack).getColor() + iter.next());
        while (iter.hasNext()) {
            tooltip.add(TextFormatting.GRAY + iter.next());
        }
    }

    public static ItemStack getItemForBlock(IBlockState state) {
        Block block = state.getBlock();
        Item item = Item.getItemFromBlock(block);
        return new ItemStack(item, 1, item.getMetadata(block.getMetaFromState(state))); // probably works?
    }

}
