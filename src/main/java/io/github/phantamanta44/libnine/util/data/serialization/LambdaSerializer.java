package io.github.phantamanta44.libnine.util.data.serialization;

import io.github.phantamanta44.libnine.util.function.ITriConsumer;
import io.github.phantamanta44.libnine.util.helper.ByteUtils;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LambdaSerializer<T> implements ISerializationProvider<T> {

    private final Class<T> type;
    private final ITriConsumer<NBTTagCompound, String, T> writerNbt;
    private final BiFunction<NBTTagCompound, String, T> readerNbt;
    private final BiConsumer<ByteUtils.Writer, T> writerBytes;
    private final Function<ByteUtils.Reader, T> readerBytes;

    public LambdaSerializer(Class<T> type,
                            ITriConsumer<NBTTagCompound, String, T> writerNbt, BiFunction<NBTTagCompound, String, T> readerNbt,
                            BiConsumer<ByteUtils.Writer, T> writerBytes, Function<ByteUtils.Reader, T> readerBytes) {
        this.type = type;
        this.writerNbt = writerNbt;
        this.readerNbt = readerNbt;
        this.writerBytes = writerBytes;
        this.readerBytes = readerBytes;
    }

    @Override
    public Class<T> getSerializationType() {
        return type;
    }

    @Override
    public void serializeNBT(T obj, String key, NBTTagCompound tag) {
        writerNbt.accept(tag, key, obj);
    }

    @Override
    public T deserializeNBT(String key, NBTTagCompound tag) {
        return readerNbt.apply(tag, key);
    }

    @Override
    public void serializeBytes(T obj, ByteUtils.Writer data) {
        writerBytes.accept(data, obj);
    }

    @Override
    public T deserializeBytes(ByteUtils.Reader data) {
        return readerBytes.apply(data);
    }

}
