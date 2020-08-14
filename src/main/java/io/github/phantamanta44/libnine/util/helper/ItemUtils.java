package io.github.phantamanta44.libnine.util.helper;

import io.github.phantamanta44.libnine.item.L9ItemSubs;
import io.github.phantamanta44.libnine.util.IDisplayableMatcher;
import io.github.phantamanta44.libnine.util.world.WorldBlockPos;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;

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
        Block block = pos.getBlock();
        return Item.getItemFromBlock(block)
                .getTranslationKey(block.getPickBlock(pos.getBlockState(), null, pos.getWorld(), pos.getPos(), null));
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
        return item.getForgeRarity(stack).getColor() + item.getItemStackDisplayName(stack);
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

}
