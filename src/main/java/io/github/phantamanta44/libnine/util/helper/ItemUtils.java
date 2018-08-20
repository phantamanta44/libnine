package io.github.phantamanta44.libnine.util.helper;

import io.github.phantamanta44.libnine.item.L9ItemSubs;
import io.github.phantamanta44.libnine.util.world.WorldBlockPos;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import java.util.function.Predicate;

public class ItemUtils {

    public static final short WILDCARD_META = (short)32767;

    public static boolean matchesWithWildcard(ItemStack a, ItemStack b) {
        //noinspection ConstantConditions
        return a.getItem().equals(b.getItem())
                && (a.getMetadata() == WILDCARD_META || b.getMetadata() == WILDCARD_META || a.getMetadata() == b.getMetadata())
                && (a.hasTagCompound() ? (b.hasTagCompound() && b.getTagCompound().equals(a.getTagCompound())) : !b.hasTagCompound());
    }

    public static Predicate<ItemStack> matchesWithWildcard(ItemStack stack) {
        return s -> matchesWithWildcard(stack, s);
    }

    public static String getColouredName(ItemStack stack) {
        return stack.getRarity().rarityColor + stack.getDisplayName();
    }

    public static String getUnlocalizedBlockName(WorldBlockPos pos) {
        Block block = pos.getBlock();
        return Item.getItemFromBlock(block)
                .getUnlocalizedName(block.getPickBlock(pos.getBlockState(), null, pos.getWorld(), pos.getPos(), null));
    }

    public static String getLocalizedBlockName(WorldBlockPos pos) {
        Block block = pos.getBlock();
        return Item.getItemFromBlock(block)
                .getItemStackDisplayName(block.getPickBlock(pos.getBlockState(), null, pos.getWorld(), pos.getPos(), null));
    }

    public static String getColouredBlockName(WorldBlockPos pos) {
        Block block = pos.getBlock();
        Item item = Item.getItemFromBlock(block);
        ItemStack stack = block.getPickBlock(pos.getBlockState(), null, pos.getWorld(), pos.getPos(), null);
        return item.getRarity(stack).rarityColor + item.getItemStackDisplayName(stack);
    }

    @SuppressWarnings("deprecation")
    public static String getLocalizedSubName(L9ItemSubs item, int meta) {
        return I18n.translateToLocal(String.format("item.%s%d.name", item.getRegistryName(), meta));
    }

}
