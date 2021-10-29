package io.github.phantamanta44.libnine.util.helper;

import io.github.phantamanta44.libnine.util.collection.Accrue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public static IItemHandlerModifiable restrict(IItemHandlerModifiable delegate,
                                                  boolean allowInsert, boolean allowExtract) {
        return new RestrictedItemHandler(delegate, allowInsert, allowExtract);
    }

    @Deprecated
    public static IItemHandlerModifiable insertOnly(IItemHandlerModifiable delegate) {
        return restrict(delegate, true, false);
    }

    @Deprecated
    public static IItemHandlerModifiable extractOnly(IItemHandlerModifiable delegate) {
        return restrict(delegate, false, true);
    }

    public static IItemHandler join(IItemHandler... handlers) {
        return join(Arrays.asList(handlers));
    }

    public static IItemHandler join(List<? extends IItemHandler> handlers) {
        return new AppendingItemHandler(handlers);
    }

    private static class RestrictedItemHandler implements IItemHandlerModifiable {

        private final IItemHandlerModifiable delegate;
        private final boolean allowInsert, allowExtract;

        private RestrictedItemHandler(IItemHandlerModifiable delegate, boolean allowInsert, boolean allowExtract) {
            this.delegate = delegate;
            this.allowInsert = allowInsert;
            this.allowExtract = allowExtract;
        }

        @Override
        public int getSlots() {
            return delegate.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return delegate.getStackInSlot(slot);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            delegate.setStackInSlot(slot, stack);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return allowInsert ? delegate.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return allowExtract ? delegate.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
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

    private static class AppendingItemHandler implements IItemHandler {

        private final List<DelegateSlot> slots;

        public AppendingItemHandler(List<? extends IItemHandler> delegates) {
            this.slots = new ArrayList<>();
            for (IItemHandler delegate : delegates) {
                for (int i = 0; i < delegate.getSlots(); i++) {
                    slots.add(new DelegateSlot(delegate, i));
                }
            }
        }

        @Override
        public int getSlots() {
            return slots.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slots.get(slot).getStackInSlot();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slots.get(slot).insertItem(stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slots.get(slot).extractItem(amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slots.get(slot).getSlotLimit();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slots.get(slot).isItemValid(stack);
        }

        private static class DelegateSlot {

            private final IItemHandler delegate;
            private final int slotIndex;

            public DelegateSlot(IItemHandler delegate, int slotIndex) {
                this.delegate = delegate;
                this.slotIndex = slotIndex;
            }

            public ItemStack getStackInSlot() {
                return delegate.getStackInSlot(slotIndex);
            }

            public ItemStack insertItem(ItemStack stack, boolean simulate) {
                return delegate.insertItem(slotIndex, stack, simulate);
            }

            public ItemStack extractItem(int amount, boolean simulate) {
                return delegate.extractItem(slotIndex, amount, simulate);
            }

            public int getSlotLimit() {
                return delegate.getSlotLimit(slotIndex);
            }

            public boolean isItemValid(ItemStack stack) {
                return delegate.isItemValid(slotIndex, stack);
            }

        }

    }

}
