package io.github.phantamanta44.libnine.util.data.serialization;

import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.util.LazyConstant;
import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.github.phantamanta44.libnine.util.data.ISerializable;
import io.github.phantamanta44.libnine.util.format.FormatUtils;
import io.github.phantamanta44.libnine.util.helper.MirrorUtils;
import io.github.phantamanta44.libnine.util.math.Vec2i;
import io.github.phantamanta44.libnine.util.nbt.NBTUtils;
import io.github.phantamanta44.libnine.util.world.WorldBlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataSerialization {

    private static final Collection<ISerializationProvider<?>> DEFAULT_SERIALIZATION_PROVIDERS = Arrays.asList(
            new LambdaSerializer<>(Integer.class,
                    NBTTagCompound::setInteger, NBTTagCompound::getInteger,
                    ByteUtils.Writer::writeInt, ByteUtils.Reader::readInt),
            new LambdaSerializer<>(Float.class,
                    NBTTagCompound::setFloat, NBTTagCompound::getFloat,
                    ByteUtils.Writer::writeFloat, ByteUtils.Reader::readFloat),
            new LambdaSerializer<>(Double.class,
                    NBTTagCompound::setDouble, NBTTagCompound::getDouble,
                    ByteUtils.Writer::writeDouble, ByteUtils.Reader::readDouble),
            new LambdaSerializer<>(Byte.class,
                    NBTTagCompound::setByte, NBTTagCompound::getByte,
                    ByteUtils.Writer::writeByte, ByteUtils.Reader::readByte),
            new LambdaSerializer<>(Short.class,
                    NBTTagCompound::setShort, NBTTagCompound::getShort,
                    ByteUtils.Writer::writeShort, ByteUtils.Reader::readShort),
            new LambdaSerializer<>(Long.class,
                    NBTTagCompound::setLong, NBTTagCompound::getLong,
                    ByteUtils.Writer::writeLong, ByteUtils.Reader::readLong),
            new LambdaSerializer<>(Boolean.class,
                    NBTTagCompound::setBoolean, NBTTagCompound::getBoolean,
                    ByteUtils.Writer::writeBool, ByteUtils.Reader::readBool),
            new LambdaSerializer<>(String.class,
                    NBTTagCompound::setString, NBTTagCompound::getString,
                    ByteUtils.Writer::writeString, ByteUtils.Reader::readString),
            new LambdaSerializer<>(NBTTagCompound.class,
                    NBTTagCompound::setTag, NBTTagCompound::getCompoundTag,
                    ByteUtils.Writer::writeTagCompound, ByteUtils.Reader::readTagCompound),
            new LambdaSerializer<>(ItemStack.class,
                    (t, k, s) -> {
                        NBTTagCompound i = new NBTTagCompound();
                        s.writeToNBT(i);
                        t.setTag(k, i);
                    }, (t, k) -> new ItemStack(t.getCompoundTag(k)),
                    ByteUtils.Writer::writeItemStack, ByteUtils.Reader::readItemStack),
            new LambdaSerializer<>(Fluid.class,
                    (t, k, f) -> t.setString(k, f.getName()), (t, k) -> FluidRegistry.getFluid(t.getString(k)),
                    ByteUtils.Writer::writeFluid, ByteUtils.Reader::readFluid),
            new LambdaSerializer<>(FluidStack.class,
                    (t, k, s) -> {
                        NBTTagCompound f = new NBTTagCompound();
                        s.writeToNBT(f);
                        t.setTag(k, f);
                    }, (t, k) -> FluidStack.loadFluidStackFromNBT(t.getCompoundTag(k)),
                    ByteUtils.Writer::writeFluidStack, ByteUtils.Reader::readFluidStack),
            new LambdaSerializer<>(BlockPos.class,
                    (t, k, p) -> t.setTag(k, NBTUtils.serializeBlockPos(p)),
                    (t, k) -> NBTUtils.deserializeBlockPos(t.getCompoundTag(k)),
                    ByteUtils.Writer::writeBlockPos, ByteUtils.Reader::readBlockPos),
            new LambdaSerializer<>(WorldBlockPos.class,
                    (t, k, p) -> t.setTag(k, NBTUtils.serializeWorldBlockPos(p)),
                    (t, k) -> NBTUtils.deserializeWorldBlockPos(t.getCompoundTag(k)),
                    ByteUtils.Writer::writeWorldBlockPos, ByteUtils.Reader::readWorldBlockPos),
            new LambdaSerializer<>(Vec3d.class,
                    (t, k, v) -> t.setTag(k, NBTUtils.serializeVec3d(v)),
                    (t, k) -> NBTUtils.deserializeVec3d(t.getCompoundTag(k)),
                    ByteUtils.Writer::writeVec3d, ByteUtils.Reader::readVec3d),
            new LambdaSerializer<>(Vec2i.class,
                    (t, k, v) -> t.setTag(k, NBTUtils.serializeVec2i(v)),
                    (t, k) -> NBTUtils.deserializeVec2i(t.getCompoundTag(k)),
                    ByteUtils.Writer::writeVec2i, ByteUtils.Reader::readVec2i),
            new LambdaSerializer<>(UUID.class,
                    (t, k, i) -> t.setString(k, i.toString()),
                    (t, k) -> UUID.fromString(t.getString(k)),
                    ByteUtils.Writer::writeUuid, ByteUtils.Reader::readUuid)
    );

    private static final Map<Class<?>, List<Field>> classMappings = new IdentityHashMap<>();

    private static List<Field> getOrComputeClassMappings(Class<?> src) {
        List<Field> mappings = classMappings.get(src);
        if (mappings == null) {
            LibNine.LOGGER.info("Calculating serialization mappings for class: {}", src.getName());
            mappings = MirrorUtils.getHierarchy(src).stream()
                    .flatMap(c -> Arrays.stream(c.getDeclaredFields()))
                    .filter(f -> f.isAnnotationPresent(AutoSerialize.class))
                    .sorted(Comparator.comparing(Field::getName))
                    .peek(f -> f.setAccessible(true))
                    .collect(Collectors.toList());
            classMappings.put(src, mappings);
        }
        return mappings;
    }

    private final Object target;
    private final Map<Class<?>, ISerializationProvider<?>> serializers;
    private final List<Supplier<DataProperty>> properties;

    public DataSerialization(Object target, Collection<ISerializationProvider<?>> serializationProviders) {
        this.target = target;
        this.serializers = new IdentityHashMap<>();
        for (ISerializationProvider<?> provider : serializationProviders) {
            serializers.put(provider.getSerializationType(), provider);
        }
        this.properties = getOrComputeClassMappings(target.getClass()).stream()
                .<Supplier<DataProperty>>map(f -> new LazyConstant<>(() -> {
                    Class<?> type = f.getType();
                    if (IDatum.class.isAssignableFrom(type)) {
                        try {
                            return new DataPropertyDatum<>(
                                    f.getAnnotation(AutoSerialize.class), f.getName(), (IDatum<?>)f.get(target));
                        } catch (Exception e) {
                            throw new IllegalStateException("Could not read field: " + f.toString(), e);
                        }
                    } else if (ISerializable.class.isAssignableFrom(type)) {
                        try {
                            return new DataPropertySerializable(
                                    f.getAnnotation(AutoSerialize.class), f.getName(), (ISerializable)f.get(target));
                        } catch (Exception e) {
                            throw new IllegalStateException("Could not read field: " + f.toString(), e);
                        }
                    } else {
                        return new DataPropertyMutableField<>(f.getAnnotation(AutoSerialize.class), f.getName(), f);
                    }
                }))
                .collect(Collectors.toList());
    }

    public DataSerialization(Object target) {
        this(target, DEFAULT_SERIALIZATION_PROVIDERS);
    }

    private Stream<DataProperty> resolveProperties() {
        return properties.stream().map(Supplier::get);
    }

    public void serializeNBT(NBTTagCompound tag) {
        resolveProperties().forEach(prop -> prop.serializeNBT(tag));
    }

    public void deserializeNBT(NBTTagCompound tag) {
        resolveProperties().forEach(prop -> prop.deserializeNBT(tag));
    }

    public void serializeBytes(ByteUtils.Writer data) {
        serializeBytes(data, true);
    }

    public void serializeBytes(ByteUtils.Writer data, boolean sync) {
        Stream<DataProperty> props = resolveProperties();
        (sync ? props.filter(prop -> prop.sync) : props).forEach(prop -> prop.serializeBytes(data));
    }

    public void deserializeBytes(ByteUtils.Reader data) {
        deserializeBytes(data, true);
    }

    public void deserializeBytes(ByteUtils.Reader data, boolean sync) {
        Stream<DataProperty> props = resolveProperties();
        (sync ? props.filter(prop -> prop.sync) : props).forEach(prop -> prop.deserializeBytes(data));
    }

    @SuppressWarnings("unchecked")
    private <T> ISerializationProvider<T> getSerializationProvider(Class<T> clazz) {
        ISerializationProvider<T> serializer = (ISerializationProvider<T>)serializers.get(clazz);
        if (serializer == null) {
            throw new UnsupportedOperationException("No serializer for type: " + clazz.getName());
        }
        return serializer;
    }

    private static abstract class DataProperty {

        final String key;
        final boolean sync;

        DataProperty(AutoSerialize annot, String fallbackName) {
            String name = annot.value();
            this.key = FormatUtils.toTitleCase(name.isEmpty() ? fallbackName : name);
            this.sync = annot.sync();
        }

        abstract void serializeNBT(NBTTagCompound tag);

        abstract void deserializeNBT(NBTTagCompound tag);

        abstract void serializeBytes(ByteUtils.Writer data);

        abstract void deserializeBytes(ByteUtils.Reader data);

    }

    private static class DataPropertySerializable extends DataProperty {

        private final ISerializable datum;

        DataPropertySerializable(AutoSerialize annot, String fallbackName, ISerializable datum) {
            super(annot, fallbackName);
            this.datum = datum;
        }

        @Override
        public void serializeNBT(NBTTagCompound tag) {
            NBTTagCompound serTag = new NBTTagCompound();
            datum.serNBT(serTag);
            tag.setTag(key, serTag);
        }

        @Override
        public void deserializeNBT(NBTTagCompound tag) {
            datum.deserNBT(tag.getCompoundTag(key));
        }

        @Override
        public void serializeBytes(ByteUtils.Writer data) {
            datum.serBytes(data);
        }

        @Override
        public void deserializeBytes(ByteUtils.Reader data) {
            datum.deserBytes(data);
        }

    }

    private class DataPropertyDatum<T> extends DataProperty {

        private final IDatum<T> datum;
        @Nullable
        private Class<T> dataClass = null;

        DataPropertyDatum(AutoSerialize annot, String fallbackName, IDatum<T> datum) {
            super(annot, fallbackName);
            this.datum = datum;
        }

        @SuppressWarnings("unchecked")
        private Class<T> getDataClass() {
            if (dataClass == null) {
                dataClass = (Class<T>)datum.get().getClass();
            }
            return dataClass;
        }

        @Override
        public void serializeNBT(NBTTagCompound tag) {
            if (getDataClass().isEnum()) {
                tag.setShort(key, (short)((Enum<?>)datum.get()).ordinal());
            } else {
                getSerializationProvider(getDataClass()).serializeNBT(datum.get(), key, tag);
            }
        }

        @Override
        public void deserializeNBT(NBTTagCompound tag) {
            if (getDataClass().isEnum()) {
                datum.set(getDataClass().getEnumConstants()[tag.getShort(key)]);
            } else {
                datum.set(getSerializationProvider(getDataClass()).deserializeNBT(key, tag));
            }
        }

        @Override
        public void serializeBytes(ByteUtils.Writer data) {
            if (getDataClass().isEnum()) {
                data.writeShort((short)((Enum<?>)datum.get()).ordinal());
            } else {
                getSerializationProvider(getDataClass()).serializeBytes(datum.get(), data);
            }
        }

        @Override
        public void deserializeBytes(ByteUtils.Reader data) {
            if (getDataClass().isEnum()) {
                datum.set(getDataClass().getEnumConstants()[data.readShort()]);
            } else {
                datum.set(getSerializationProvider(getDataClass()).deserializeBytes(data));
            }
        }

    }

    private class DataPropertyMutableField<T> extends DataProperty {

        private final Field field;
        private final Class<T> dataClass;

        @SuppressWarnings("unchecked")
        DataPropertyMutableField(AutoSerialize annot, String fallbackName, Field field) {
            super(annot, fallbackName);
            this.field = field;
            this.dataClass = (Class<T>)field.getType(); // why isn't Field parametric???
        }

        @SuppressWarnings("unchecked")
        private T fieldGet() {
            try {
                return (T)field.get(target);
            } catch (Exception e) {
                throw new IllegalStateException("Could not read field: " + field.toString(), e);
            }
        }

        private void fieldSet(T value) {
            try {
                field.set(target, value);
            } catch (Exception e) {
                throw new IllegalStateException("Could not write field: " + field.toString(), e);
            }
        }

        @Override
        void serializeNBT(NBTTagCompound tag) {
            if (dataClass.isEnum()) {
                tag.setShort(key, (short)((Enum<?>)fieldGet()).ordinal());
            } else {
                getSerializationProvider(dataClass).serializeNBT(fieldGet(), key, tag);
            }
        }

        @Override
        void deserializeNBT(NBTTagCompound tag) {
            if (dataClass.isEnum()) {
                fieldSet(dataClass.getEnumConstants()[tag.getShort(key)]);
            } else {
                fieldSet(getSerializationProvider(dataClass).deserializeNBT(key, tag));
            }
        }

        @Override
        void serializeBytes(ByteUtils.Writer data) {
            if (dataClass.isEnum()) {
                data.writeShort((short)((Enum<?>)fieldGet()).ordinal());
            } else {
                getSerializationProvider(dataClass).serializeBytes(fieldGet(), data);
            }
        }

        @Override
        void deserializeBytes(ByteUtils.Reader data) {
            if (dataClass.isEnum()) {
                fieldSet(dataClass.getEnumConstants()[data.readShort()]);
            } else {
                fieldSet(getSerializationProvider(dataClass).deserializeBytes(data));
            }
        }

    }

}
