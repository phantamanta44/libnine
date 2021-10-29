package io.github.phantamanta44.libnine.util.gameobject;

import io.github.phantamanta44.libnine.util.helper.OreDictUtils;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class ItemIdentity {

    public static final ItemIdentity AIR = new ItemIdentity(Items.AIR);

    public static ItemIdentity getForStack(ItemStack stack) {
        return stack.isEmpty() ? AIR : new ItemIdentity(
                stack.getItem(), stack.getMetadata(), stack.hasTagCompound() ? stack.getTagCompound() : null);
    }

    private final Item item;
    private final int meta;
    @Nullable
    private final NBTTagCompound dataTag;

    public ItemIdentity(Item item, int meta, @Nullable NBTTagCompound dataTag) {
        this.item = item;
        this.meta = meta;
        this.dataTag = dataTag;
    }

    public ItemIdentity(Item item, int meta) {
        this(item, meta, null);
    }

    public ItemIdentity(Item item) {
        this(item, OreDictionary.WILDCARD_VALUE);
    }

    public Item getItem() {
        return item;
    }

    public int getMeta() {
        return meta;
    }

    @Nullable
    public NBTTagCompound getDataTag() {
        return dataTag != null ? dataTag.copy() : null;
    }

    public boolean isAir() {
        return item == Items.AIR;
    }

    public ItemIdentity mutate(Consumer<ItemStack> mutator) {
        ItemStack stack = createStack(1);
        mutator.accept(stack);
        return getForStack(stack);
    }

    public ItemStack createStack(int count) {
        if (isAir()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item, count, meta == OreDictionary.WILDCARD_VALUE ? 0 : meta);
        if (dataTag != null) {
            stack.setTagCompound(dataTag.copy());
        }
        return stack;
    }

    public boolean matches(ItemStack stack) {
        if (isAir()) {
            return stack.isEmpty();
        }
        if (stack.getItem() != item || (meta != OreDictionary.WILDCARD_VALUE && stack.getMetadata() != meta)) {
            return false;
        }
        return dataTag == null ? !stack.hasTagCompound()
                : (stack.hasTagCompound() && dataTag.equals(Objects.requireNonNull(stack.getTagCompound())));
    }

    public boolean matchesOreDict(String oreName) {
        return OreDictUtils.matchesOredict(createStack(1), oreName);
    }

    @Override
    public int hashCode() {
        if (isAir()) {
            return 0;
        }
        return (item.hashCode() * 523) ^ (meta * 17) ^ (dataTag == null ? 0xDEADBEEF : dataTag.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemIdentity)) {
            return false;
        }
        ItemIdentity o = (ItemIdentity)obj;
        return isAir() && o.isAir() || (item == o.item && meta == o.meta && Objects.equals(dataTag, o.dataTag));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(item.getRegistryName()).append(":").append(meta);
        if (dataTag != null) {
            sb.append(" ").append(dataTag);
        }
        return sb.toString();
    }

}
