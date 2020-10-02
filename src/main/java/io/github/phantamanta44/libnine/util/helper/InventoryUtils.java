package io.github.phantamanta44.libnine.util.helper;

import io.github.phantamanta44.libnine.util.collection.Accrue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
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

    public static IItemHandlerModifiable insertOnly(IItemHandlerModifiable delegate) {
        return new InsertOnlyItemHandler(delegate);
    }

    public static IItemHandlerModifiable extractOnly(IItemHandlerModifiable delegate) {
        return new ExtractOnlyItemHandler(delegate);
    }

    private static class InsertOnlyItemHandler implements IItemHandlerModifiable {

        private final IItemHandlerModifiable delegate;

        InsertOnlyItemHandler(IItemHandlerModifiable delegate) {
            this.delegate = delegate;
        }

        @Override
        public int getSlots() {
            return delegate.getSlots();
        }

        @Override
        @Nonnull
        public ItemStack getStackInSlot(int slot) {
            return delegate.getStackInSlot(slot);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            delegate.setStackInSlot(slot, stack);
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return delegate.insertItem(slot, stack, simulate);
        }

        @Override
        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return delegate.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return delegate.isItemValid(slot, stack);
        }

    }

    private static class ExtractOnlyItemHandler implements IItemHandlerModifiable {

        private final IItemHandlerModifiable delegate;

        ExtractOnlyItemHandler(IItemHandlerModifiable delegate) {
            this.delegate = delegate;
        }

        @Override
        public int getSlots() {
            return delegate.getSlots();
        }

        @Override
        @Nonnull
        public ItemStack getStackInSlot(int slot) {
            return delegate.getStackInSlot(slot);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            delegate.setStackInSlot(slot, stack);
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return delegate.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return delegate.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

    }

}
