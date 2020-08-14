package io.github.phantamanta44.libnine.util.helper;

import io.github.phantamanta44.libnine.util.IDisplayableMatcher;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.List;

public class OreDictUtils {

    @Nullable
    public static ItemStack getStack(String entry, int qty) {
        List<ItemStack> items = OreDictionary.getOres(entry, false);
        if (items.isEmpty()) return null;
        ItemStack stack = items.get(0).copy();
        stack.setCount(qty);
        return stack;
    }

    public static boolean exists(String entry) {
        return !OreDictionary.getOres(entry, false).isEmpty();
    }

    public static boolean matchesOredict(ItemStack stack, String ore) {
        if (stack.isEmpty()) return false;
        int targetId = OreDictionary.getOreID(ore);
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (id == targetId) return true;
        }
        return false;
    }

    public static IDisplayableMatcher<ItemStack> matchesOredict(String ore) {
        return IDisplayableMatcher.ofMany(() -> OreDictionary.getOres(ore, false), s -> matchesOredict(s, ore));
    }

}
