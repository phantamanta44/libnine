package io.github.phantamanta44.libnine.util.helper;

import io.github.phantamanta44.libnine.util.collection.Accrue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class InventoryUtils {

    public static Stream<ItemStack> stream(IInventory inv) {
        return IntStream.range(0, inv.getSizeInventory()).mapToObj(inv::getStackInSlot);
    }

    public static Stream<ItemStack> stream(IItemHandler inv) {
        return IntStream.range(0, inv.getSlots()).mapToObj(inv::getStackInSlot);
    }

    public static Stream<ItemStack> streamInventory(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            return stream(((EntityPlayer)entity).inventory);
        } else {
            return OptUtils.capability(entity, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .map(InventoryUtils::stream)
                    .orElseGet(() -> StreamSupport.stream(entity.getEquipmentAndArmor().spliterator(), false));
        }
    }

    public static void accrue(Accrue<ItemStack> accum, IItemHandler... invs) {
        for (IItemHandler inv : invs) {
            stream(inv).forEach(accum);
        }
    }

}
