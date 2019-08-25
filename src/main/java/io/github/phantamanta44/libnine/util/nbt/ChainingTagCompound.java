package io.github.phantamanta44.libnine.util.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import scala.actors.threadpool.Arrays;

import java.util.List;
import java.util.function.Consumer;

public class ChainingTagCompound extends NBTTagCompound {

    public ChainingTagCompound() {
        // NO-OP
    }

    public ChainingTagCompound(NBTTagCompound base) {
        for (String key : base.getKeySet()) setTag(key, base.getTag(key));
    }

    public ChainingTagCompound withTag(String key, NBTTagCompound tag) {
        super.setTag(key, tag);
        return this;
    }

    public ChainingTagCompound withInt(String key, int value) {
        super.setInteger(key, value);
        return this;
    }

    public ChainingTagCompound withByte(String key, int value) {
        super.setByte(key, (byte)value);
        return this;
    }

    public ChainingTagCompound withFloat(String key, float value) {
        super.setFloat(key, value);
        return this;
    }

    public ChainingTagCompound withDouble(String key, double value) {
        super.setDouble(key, value);
        return this;
    }

    public ChainingTagCompound withBool(String key, boolean value) {
        super.setBoolean(key, value);
        return this;
    }

    public ChainingTagCompound withStr(String key, String value) {
        super.setString(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends NBTBase> ChainingTagCompound withList(String key, T... tags) {
        return withList(key, Arrays.asList(tags));
    }

    public <T extends NBTBase> ChainingTagCompound withList(String key, List<T> tags) {
        NBTTagList list = new NBTTagList();
        for (T tag : tags) {
            list.appendTag(tag);
        }
        return withList(key, list);
    }

    public ChainingTagCompound withList(String key, NBTTagList list) {
        setTag(key, list);
        return this;
    }

    public ChainingTagCompound withItemStack(String key, ItemStack value) {
        return withSerializable(key, value::writeToNBT);
    }

    public ChainingTagCompound withFluidStack(String key, FluidStack value) {
        return withSerializable(key, value::writeToNBT);
    }

    public ChainingTagCompound withSerializable(String key, Consumer<NBTTagCompound> writer) {
        super.setTag(key, new NBTTagCompound());
        writer.accept(super.getCompoundTag(key));
        return this;
    }

}
