package io.github.phantamanta44.libnine.util.nbt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class NBTUtils {

    public static ChainingTagCompound serializeBlockPos(BlockPos pos) {
        return new ChainingTagCompound()
                .withInt("x", pos.getX()).withInt("y", pos.getY()).withInt("z", pos.getZ());
    }

    public static BlockPos deserializeBlockPos(NBTTagCompound tag) {
        return new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
    }

    public static ChainingTagCompound serializeVector(Vec3d vec) {
        return new ChainingTagCompound()
                .withDouble("x", vec.x).withDouble("y", vec.y).withDouble("z", vec.z);
    }

    public static Vec3d deserializeVector(NBTTagCompound tag) {
        return new Vec3d(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
    }

}
