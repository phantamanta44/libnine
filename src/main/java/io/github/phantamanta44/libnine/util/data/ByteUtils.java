package io.github.phantamanta44.libnine.util.data;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.phantamanta44.libnine.util.ImpossibilityRealizedException;
import io.github.phantamanta44.libnine.util.helper.ResourceUtils;
import io.github.phantamanta44.libnine.util.math.Vec2i;
import io.github.phantamanta44.libnine.util.world.WorldBlockPos;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

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
            for (int k = 0; k < bytes.length; k++) bytes[k] = (byte)((i & (0xFF << (k * 8))) >> (k * 8));
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
            for (int k = 0; k < bytes.length; k++) bytes[k] = (byte)((i & (0xFF << (k * 8))) >> (k * 8));
            return writeBytes(bytes);
        }

        public Writer writeLong(long i) {
            byte[] bytes = new byte[Long.BYTES];
            for (int k = 0; k < bytes.length; k++) bytes[k] = (byte)((i & (0xFFL << (k * 8))) >> (k * 8));
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
            byte[] bytes = s.getBytes(ResourceUtils.UTF_8);
            return writeVarPrecision(bytes.length).writeBytes(bytes);
        }

        public Writer writeTagCompound(NBTTagCompound tag) {
            try {
                ByteArrayDataOutput buf = ByteStreams.newDataOutput();

                CompressedStreamTools.write(tag, buf);
                byte[] bytes = buf.toByteArray();
                return writeVarPrecision(bytes.length).writeBytes(bytes);
            } catch (IOException e) {
                throw new ImpossibilityRealizedException(e);
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
        
        public Writer writeBlockPos(BlockPos pos) {
            return writeInt(pos.getX()).writeInt(pos.getY()).writeInt(pos.getZ());
        }

        public Writer writeWorldBlockPos(WorldBlockPos pos) {
            return writeInt(pos.getDimId()).writeBlockPos(pos.getPos());
        }
        
        public Writer writeVec3d(Vec3d vec) {
            return writeDouble(vec.x).writeDouble(vec.y).writeDouble(vec.z);
        }
        
        public Writer writeVec2i(Vec2i vec) {
            return writeInt(vec.getX()).writeInt(vec.getY());
        }
        
        public Writer writeUuid(UUID id) {
            return writeString(id.toString());
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
                value |= (Byte.toUnsignedLong(data[pointer + i]) << (i * 8));
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
            return new String(data, pointer - length, length, ResourceUtils.UTF_8);
        }

        public NBTTagCompound readTagCompound() {
            try {
                int length = readVarPrecision();
                NBTTagCompound tag = CompressedStreamTools.read(
                        ByteStreams.newDataInput(readBytes(length)),
                        new NBTSizeTracker(length));
                return tag;
            } catch (Exception e) {
                throw new ImpossibilityRealizedException(e);
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

        public BlockPos readBlockPos() {
            return new BlockPos(readInt(), readInt(), readInt());
        }

        public WorldBlockPos readWorldBlockPos() {
            return new WorldBlockPos(readInt(), readBlockPos());
        }

        public Vec3d readVec3d() {
            return new Vec3d(readDouble(), readDouble(), readDouble());
        }

        public Vec2i readVec2i() {
            return new Vec2i(readInt(), readInt());
        }

        public UUID readUuid() {
            return UUID.fromString(readString());
        }

    }

}
