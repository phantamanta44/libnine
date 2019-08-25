package io.github.phantamanta44.libnine.util.nbt;

import io.github.phantamanta44.libnine.util.math.Vec2i;
import io.github.phantamanta44.libnine.util.world.WorldBlockPos;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.stream.Collector;

public class NBTUtils {

    public static ChainingTagCompound serializeBlockPos(BlockPos pos) {
        return new ChainingTagCompound()
                .withInt("x", pos.getX()).withInt("y", pos.getY()).withInt("z", pos.getZ());
    }

    public static BlockPos deserializeBlockPos(NBTTagCompound tag) {
        return new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
    }

    public static ChainingTagCompound serializeWorldBlockPos(WorldBlockPos pos) {
        return new ChainingTagCompound()
                .withInt("dim", pos.getDimId()).withTag("pos", serializeBlockPos(pos.getPos()));
    }

    public static WorldBlockPos deserializeWorldBlockPos(NBTTagCompound tag) {
        return new WorldBlockPos(tag.getInteger("dim"), deserializeBlockPos(tag.getCompoundTag("pos")));
    }

    public static ChainingTagCompound serializeVec3d(Vec3d vec) {
        return new ChainingTagCompound()
                .withDouble("x", vec.x).withDouble("y", vec.y).withDouble("z", vec.z);
    }

    public static Vec3d deserializeVec3d(NBTTagCompound tag) {
        return new Vec3d(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
    }

    public static ChainingTagCompound serializeVec2i(Vec2i vec) {
        return new ChainingTagCompound()
                .withInt("x", vec.getX()).withInt("y", vec.getY());
    }

    public static Vec2i deserializeVec2i(NBTTagCompound tag) {
        return new Vec2i(tag.getInteger("x"), tag.getInteger("y"));
    }

    public static <T extends NBTBase> Collector<T, NBTTagList, NBTTagList> collectList() {
        return Collector.of(NBTTagList::new, NBTTagList::appendTag, NBTUtils::mergeLists,
                Collector.Characteristics.IDENTITY_FINISH);
    }

    public static NBTTagList mergeLists(NBTTagList a, NBTTagList b) {
        NBTTagList result = new NBTTagList();
        for (NBTBase tag : a) {
            result.appendTag(tag);
        }
        for (NBTBase tag : b) {
            result.appendTag(tag);
        }
        return result;
    }

}
