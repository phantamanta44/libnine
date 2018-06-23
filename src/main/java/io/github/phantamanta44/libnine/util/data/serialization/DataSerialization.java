package io.github.phantamanta44.libnine.util.data.serialization;

import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.util.data.ISerializable;
import io.github.phantamanta44.libnine.util.helper.ByteUtils;
import io.github.phantamanta44.libnine.util.helper.FormatUtils;
import io.github.phantamanta44.libnine.util.helper.MirrorUtils;
import io.github.phantamanta44.libnine.util.tuple.IPair;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class DataSerialization {

    @SuppressWarnings("unchecked")
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
            new LambdaSerializer<>(EnumFacing.class,
                    (t, k, d) -> t.setByte(k, (byte)d.ordinal()), (t, k) -> EnumFacing.values()[t.getByte(k)],
                    ByteUtils.Writer::writeDir, ByteUtils.Reader::readDir),
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
                    ByteUtils.Writer::writeFluidStack, ByteUtils.Reader::readFluidStack)
    );

    private static final Map<Class<?>, List<Field>> classMappings = new HashMap<>();

    private static void calculateClassMappings(Class<?> src) {
        LibNine.LOGGER.info("Calculating serialization mappings for class: {}", src.getName());
        classMappings.put(src, MirrorUtils.getHierarchy(src).stream()
                .flatMap(c -> Arrays.stream(c.getDeclaredFields()))
                .filter(f -> f.isAnnotationPresent(AutoSerialize.class))
                .sorted(Comparator.comparing(Field::getName))
                .peek(f -> f.setAccessible(true))
                .peek(f -> LibNine.LOGGER.info("- Found serializable field: {}", f.getName()))
                .collect(Collectors.toList()));
    }

    private static List<IPair<String, Object>> applyClassMappings(Object obj) {
        return classMappings.get(obj.getClass()).stream()
                .map(f -> {
                    try {
                        Object o = f.get(obj);
                        return IPair.of(f.getName(), (o instanceof IDatum || o instanceof ISerializable) ? o : f);
                    } catch (Exception e) {
                        throw new IllegalStateException("Could not read field: " + f.toString(), e);
                    }
                })
                .collect(Collectors.toList());
    }

    private final Object target;
    private final Map<Class<?>, ISerializationProvider<?>> serializers;
    
    public DataSerialization(Object target, Collection<ISerializationProvider<?>> serializationProviders) {
        this.target = target;
        if (!classMappings.containsKey(target.getClass())) calculateClassMappings(target.getClass());
        this.serializers = new HashMap<>();
        for (ISerializationProvider<?> provider : serializationProviders) {
            serializers.put(provider.getSerializationType(), provider);
        }

    }

    public DataSerialization(Object target) {
        this(target, DEFAULT_SERIALIZATION_PROVIDERS);
    }

    @SuppressWarnings("unchecked")
    public void serializeNBT(NBTTagCompound tag) {
        applyClassMappings(target).forEach(o -> {
            String key = FormatUtils.toTitleCase(o.getA());
            if (o.getB() instanceof ISerializable) {
                NBTTagCompound serTag = new NBTTagCompound();
                ((ISerializable)o.getB()).serializeNBT(serTag);
                tag.setTag(key, serTag);
            } else if (o.getB() instanceof IDatum) {
                IDatum d = (IDatum)o.getB();
                getSerializationProvider(d.get().getClass()).serializeNBT(d.get(), key, tag);
            } else {
                Field f = (Field)o.getB();
                try {
                    getSerializationProvider(f.getType()).serializeNBT(f.get(target), key, tag);
                } catch (Exception e) {
                    throw new IllegalStateException("Could not read field: " + f.toString(), e);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void deserializeNBT(NBTTagCompound tag) {
        applyClassMappings(target).forEach(o -> {
            String key = FormatUtils.toTitleCase(o.getA());
            if (o.getB() instanceof ISerializable) {
                ((ISerializable)o.getB()).deserializeNBT(tag.getCompoundTag(key));
            } else if (o.getB() instanceof IDatum) {
                IDatum d = (IDatum)o.getB();
                d.set(getSerializationProvider(d.get().getClass()).deserializeNBT(key, tag));
            } else {
                Field f = (Field)o.getB();
                try {
                    f.set(target, getSerializationProvider(f.getType()).deserializeNBT(key, tag));
                } catch (Exception e) {
                    throw new IllegalStateException("Could not write field: " + f.toString(), e);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void serializeBytes(ByteUtils.Writer data) {
        applyClassMappings(target).forEach(o -> {
            if (o.getB() instanceof ISerializable) {
                ((ISerializable)o.getB()).serializeBytes(data);
            } else if (o.getB() instanceof IDatum) {
                IDatum d = (IDatum)o.getB();
                getSerializationProvider(d.get().getClass()).serializeBytes(d.get(), data);
            } else {
                Field f = (Field)o.getB();
                try {
                    getSerializationProvider(f.getType()).serializeBytes(f.get(target), data);
                } catch (Exception e) {
                    throw new IllegalStateException("Could not read field: " + f.toString(), e);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void deserializeBytes(ByteUtils.Reader data) {
        applyClassMappings(target).forEach(o -> {
            if (o.getB() instanceof ISerializable) {
                ((ISerializable)o.getB()).deserializeBytes(data);
            } else if (o.getB() instanceof IDatum) {
                IDatum d = (IDatum)o.getB();
                d.set(getSerializationProvider(d.get().getClass()).deserializeBytes(data));
            } else {
                Field f = (Field)o.getB();
                try {
                    f.set(target, getSerializationProvider(f.getType()).deserializeBytes(data));
                } catch (Exception e) {
                    throw new IllegalStateException("Could not write field: " + f.toString(), e);
                }
            }
        });
    }

    private ISerializationProvider getSerializationProvider(Class<?> clazz) {
        ISerializationProvider serializer = serializers.get(clazz);
        if (serializer == null) {
            throw new NoSuchElementException("No serializer for type: " + clazz.getName());
        }
        return serializer;
    }
    
}
