package io.github.phantamanta44.libnine.util.data.serialization;

import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.util.LazyConstant;
import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.github.phantamanta44.libnine.util.data.ISerializable;
import io.github.phantamanta44.libnine.util.format.FormatUtils;
import io.github.phantamanta44.libnine.util.helper.MirrorUtils;
import io.github.phantamanta44.libnine.util.math.Vec2i;
import io.github.phantamanta44.libnine.util.nbt.NBTUtils;
import io.github.phantamanta44.libnine.util.tuple.IPair;
import io.github.phantamanta44.libnine.util.world.WorldBlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

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
    private final List<Supplier<IPair<String, ?>>> properties;

    public DataSerialization(Object target, Collection<ISerializationProvider<?>> serializationProviders) {
        this.target = target;
        this.serializers = new IdentityHashMap<>();
        for (ISerializationProvider<?> provider : serializationProviders) {
            serializers.put(provider.getSerializationType(), provider);
        }
        this.properties = getOrComputeClassMappings(target.getClass()).stream()
                .<Supplier<IPair<String, ?>>>map(f -> {
                    Class<?> type = f.getType();
                    if (IDatum.class.isAssignableFrom(type) || ISerializable.class.isAssignableFrom(type)) {
                        return new LazyConstant<>(() -> {
                            try {
                                return IPair.of(f.getName(), f.get(target));
                            } catch (Exception e) {
                                throw new IllegalStateException("Could not read field: " + f.toString(), e);
                            }
                        });
                    } else {
                        return () -> IPair.of(f.getName(), f);
                    }
                })
                .collect(Collectors.toList());
    }

    public DataSerialization(Object target) {
        this(target, DEFAULT_SERIALIZATION_PROVIDERS);
    }

    private Stream<IPair<String, ?>> resolveProperties() {
        return properties.stream().map(Supplier::get);
    }

    @SuppressWarnings("unchecked")
    public void serializeNBT(NBTTagCompound tag) {
        resolveProperties().forEach(o -> {
            String key = FormatUtils.toTitleCase(o.getA());
            if (o.getB() instanceof ISerializable) {
                NBTTagCompound serTag = new NBTTagCompound();
                ((ISerializable)o.getB()).serNBT(serTag);
                tag.setTag(key, serTag);
            } else if (o.getB() instanceof IDatum) {
                IDatum d = (IDatum)o.getB();
                if (d.get().getClass().isEnum()) {
                    tag.setShort(key, (short)((Enum<?>)d.get()).ordinal());
                } else {
                    getSerializationProvider(d.get().getClass()).serializeNBT(d.get(), key, tag);
                }
            } else {
                Field f = (Field)o.getB();
                try {
                    if (f.getType().isEnum()) {
                        tag.setShort(key, (short)((Enum<?>)f.get(target)).ordinal());
                    } else {
                        getSerializationProvider(f.getType()).serializeNBT(f.get(target), key, tag);
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("Could not read field: " + f.toString(), e);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void deserializeNBT(NBTTagCompound tag) {
        resolveProperties().forEach(o -> {
            String key = FormatUtils.toTitleCase(o.getA());
            if (o.getB() instanceof ISerializable) {
                ((ISerializable)o.getB()).deserNBT(tag.getCompoundTag(key));
            } else if (o.getB() instanceof IDatum) {
                IDatum d = (IDatum)o.getB();
                if (d.get().getClass().isEnum()) {
                    d.set(d.get().getClass().getEnumConstants()[tag.getShort(key)]);
                } else {
                    d.set(getSerializationProvider(d.get().getClass()).deserializeNBT(key, tag));
                }
            } else {
                Field f = (Field)o.getB();
                try {
                    if (f.getType().isEnum()) {
                        f.set(target, f.getType().getEnumConstants()[tag.getShort(key)]);
                    } else {
                        f.set(target, getSerializationProvider(f.getType()).deserializeNBT(key, tag));
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("Could not write field: " + f.toString(), e);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void serializeBytes(ByteUtils.Writer data) {
        resolveProperties().forEach(o -> {
            if (o.getB() instanceof ISerializable) {
                ((ISerializable)o.getB()).serBytes(data);
            } else if (o.getB() instanceof IDatum) {
                IDatum d = (IDatum)o.getB();
                if (d.get().getClass().isEnum()) {
                    data.writeShort((short)((Enum<?>)d.get()).ordinal());
                } else {
                    getSerializationProvider(d.get().getClass()).serializeBytes(d.get(), data);
                }
            } else {
                Field f = (Field)o.getB();
                try {
                    if (f.getType().isEnum()) {
                        data.writeShort((short)((Enum<?>)f.get(target)).ordinal());
                    } else {
                        getSerializationProvider(f.getType()).serializeBytes(f.get(target), data);
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("Could not read field: " + f.toString(), e);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void deserializeBytes(ByteUtils.Reader data) {
        resolveProperties().forEach(o -> {
            if (o.getB() instanceof ISerializable) {
                ((ISerializable)o.getB()).deserBytes(data);
            } else if (o.getB() instanceof IDatum) {
                IDatum d = (IDatum)o.getB();
                if (d.get().getClass().isEnum()) {
                    d.set(d.get().getClass().getEnumConstants()[data.readShort()]);
                } else {
                    d.set(getSerializationProvider(d.get().getClass()).deserializeBytes(data));
                }
            } else {
                Field f = (Field)o.getB();
                try {
                    if (f.getType().isEnum()) {
                        f.set(target, f.getType().getEnumConstants()[data.readShort()]);
                    } else {
                        f.set(target, getSerializationProvider(f.getType()).deserializeBytes(data));
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("Could not write field: " + f.toString(), e);
                }
            }
        });
    }

    private ISerializationProvider getSerializationProvider(Class<?> clazz) {
        ISerializationProvider serializer = serializers.get(clazz);
        if (serializer == null) {
            throw new UnsupportedOperationException("No serializer for type: " + clazz.getName());
        }
        return serializer;
    }

}
