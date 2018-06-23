package io.github.phantamanta44.libnine.util.helper;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;

public class ByteUtils {

    public static Writer writer() {
        return new Writer();
    }

    public static Reader reader(byte[] data) {
        return new Reader(data);
    }

    public static class Writer {

        private final LinkedList<byte[]> data;
        private int length;

        Writer() {
            this.data = new LinkedList<>();
            this.length = 0;
        }

        public Writer writeBytes(byte[] b) {
            data.add(b);
            length += b.length;
            return this;
        }

        public Writer writeByte(byte i) {
            data.add(new byte[] {i});
            length++;
            return this;
        }

        public Writer writeInt(int i) {
            byte[] bytes = new byte[Integer.BYTES];
            for (int k = 0; k < bytes.length; k++) bytes[k] = (byte)((i & (0xFF << (k * 8))) >> k * 8);
            return writeBytes(bytes);
        }

        public Writer writeFloat(float f) {
            return writeInt(Float.floatToRawIntBits(f));
        }

        public Writer writeDouble(double f) {
            return writeLong(Double.doubleToRawLongBits(f));
        }

        public Writer writeShort(short i) {
            byte[] bytes = new byte[Short.BYTES];
            for (int k = 0; k < bytes.length; k++) bytes[k] = (byte)((i & (0xFF << (k * 8))) >> k * 8);
            return writeBytes(bytes);
        }

        public Writer writeLong(long i) {
            byte[] bytes = new byte[Long.BYTES];
            for (int k = 0; k < bytes.length; k++) bytes[k] = (byte)((i & (0xFF << (k * 8))) >> k * 8);
            return writeBytes(bytes);
        }

        public Writer writeBool(boolean b) {
            return writeByte(b ? (byte)1 : (byte)0);
        }

        public Writer writeVarPrecision(int i) {
            while (true) {
                int afterShift = i >>> 7;
                if (afterShift == 0) {
                    writeByte((byte)((i & 0b01111111) | 0b10000000));
                    break;
                } else {
                    writeByte((byte)(i & 0b01111111));
                }
                i = afterShift;
            }
            return this;
        }

        public Writer writeString(String s) {
            byte[] bytes = s.getBytes(Charset.forName("UTF-8"));
            return writeVarPrecision(bytes.length).writeBytes(bytes);
        }

        public Writer writeDir(EnumFacing dir) {
            return writeByte((byte)dir.ordinal());
        }

        public Writer writeTagCompound(NBTTagCompound tag) {
            try {
                ByteArrayDataOutput buf = ByteStreams.newDataOutput();
                CompressedStreamTools.write(tag, buf);
                return writeBytes(buf.toByteArray());
            } catch (IOException e) {
                throw new IllegalStateException("This shouldn't happen!", e);
            }
        }

        public Writer writeItemStack(ItemStack stack) {
            if (stack.isEmpty()) return writeShort((short)-1);
            writeShort((short)Item.getIdFromItem(stack.getItem()))
                    .writeByte((byte)stack.getCount())
                    .writeShort((short)stack.getMetadata());
            if (stack.getItem().isDamageable() || stack.getItem().getShareTag()) {
                NBTTagCompound tag = stack.getItem().getNBTShareTag(stack);
                if (tag != null) return writeBool(true).writeTagCompound(tag);
            }
            return writeBool(false);
        }

        public Writer writeFluid(Fluid fluid) {
            return writeString(fluid.getName());
        }

        public Writer writeFluidStack(FluidStack stack) {
            writeFluid(stack.getFluid()).writeInt(stack.amount);
            return stack.tag != null ? writeBool(true).writeTagCompound(stack.tag) : writeBool(false);
        }

        public byte[] toArray() {
            byte[] buf = new byte[length];
            int pointer = 0;
            for (byte[] chunk : data) {
                System.arraycopy(chunk, 0, buf, pointer, chunk.length);
                pointer += chunk.length;
            }
            return buf;
        }

    }

    public static class Reader {

        private static final Field fRead;

        static {
            try {
                fRead = ReflectionHelper.findField(NBTSizeTracker.class, "c", "read");
                fRead.setAccessible(true);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to access NBTSizeTracker#read!", e);
            }
        }

        private final byte[] data;
        private int pointer;

        Reader(byte[] data) {
            this.data = data;
            this.pointer = 0;
        }

        public Reader backUp(int bytes) {
            pointer = Math.max(pointer - bytes, 0);
            return this;
        }

        public byte[] readBytes(int length) {
            pointer += length;
            return Arrays.copyOfRange(data, pointer - length, pointer);
        }

        public byte readByte() {
            return data[pointer++];
        }

        public int readInt() {
            int value = 0;
            for (int i = 0; i < Integer.BYTES; i++) {
                value |= (Byte.toUnsignedInt(data[pointer + i]) << (i * 8));
            }
            pointer += Integer.BYTES;
            return value;
        }

        public float readFloat() {
            return Float.intBitsToFloat(readInt());
        }

        public double readDouble() {
            return Double.longBitsToDouble(readLong());
        }

        public short readShort() {
            short value = 0;
            for (int i = 0; i < Short.BYTES; i++) {
                value |= (Byte.toUnsignedInt(data[pointer + i]) << (i * 8));
            }
            pointer += Short.BYTES;
            return value;
        }

        public long readLong() {
            long value = 0;
            for (int i = 0; i < Long.BYTES; i++) {
                value |= (Byte.toUnsignedInt(data[pointer + i]) << (i * 8));
            }
            pointer += Long.BYTES;
            return value;
        }

        public boolean readBool() {
            return readByte() != 0;
        }

        public int readVarPrecision() {
            int value = 0;
            int i = 0;
            byte chunk;
            do {
                chunk = readByte();
                value |= (chunk & 0b01111111) << (7 * (i++));
            } while ((chunk & 0b10000000) == 0);
            return value;
        }

        public String readString() {
            int length = readVarPrecision();
            pointer += length;
            return new String(data, pointer - length, length, Charset.forName("UTF-8"));
        }

        public EnumFacing readDir() {
            return EnumFacing.values()[readByte()];
        }

        public NBTTagCompound readTagCompound() {
            try {
                NBTSizeTracker nst = new NBTSizeTracker(2097152L);
                NBTTagCompound tag = CompressedStreamTools.read(ByteStreams.newDataInput(data, pointer), nst);
                pointer += (int)fRead.getLong(nst);
                return tag;
            } catch (Exception e) {
                throw new IllegalStateException("This shouldn't happen!", e);
            }
        }

        public ItemStack readItemStack() {
            short id = readShort();
            if (id == -1) return ItemStack.EMPTY;
            Item item = Item.getItemById(id);
            byte amount = readByte();
            short meta = readShort();
            ItemStack stack = new ItemStack(item, amount, meta);
            if (readBool()) stack.setTagCompound(readTagCompound());
            return stack;
        }

        public Fluid readFluid() {
            return FluidRegistry.getFluid(readString());
        }

        public FluidStack readFluidStack() {
            Fluid fluid = readFluid();
            int amount = readInt();
            FluidStack stack = new FluidStack(fluid, amount);
            if (readBool()) stack.tag = readTagCompound();
            return stack;
        }

    }

}
