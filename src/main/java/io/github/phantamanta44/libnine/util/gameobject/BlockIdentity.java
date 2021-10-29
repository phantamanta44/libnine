package io.github.phantamanta44.libnine.util.gameobject;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class BlockIdentity {

    public static final BlockIdentity AIR = new BlockIdentity(Blocks.AIR);

    public static BlockIdentity getForState(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.AIR ? AIR : new BlockIdentity(block, block.getMetaFromState(state));
    }

    private final Block block;
    private final int meta;

    public BlockIdentity(Block block, int meta) {
        this.block = block;
        this.meta = meta;
    }

    public BlockIdentity(Block block) {
        this(block, -1);
    }

    public Block getBlock() {
        return block;
    }

    public int getMeta() {
        return meta;
    }

    public boolean isAir() {
        return block == Blocks.AIR;
    }

    @SuppressWarnings("deprecation")
    public IBlockState createState() {
        return meta == -1 ? block.getDefaultState() : block.getStateFromMeta(meta);
    }

    public boolean matches(IBlockState state) {
        if (isAir()) {
            return state.getBlock() == Blocks.AIR;
        }
        return state.getBlock() == block && (meta == -1 || block.getMetaFromState(state) == meta);
    }

    @Override
    public int hashCode() {
        if (isAir()) {
            return 0;
        }
        return (block.hashCode() * 523) ^ meta;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlockIdentity)) {
            return false;
        }
        BlockIdentity o = (BlockIdentity)obj;
        return isAir() && o.isAir() || (block == o.block && meta == o.meta);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(block.getRegistryName());
        if (meta != -1) {
            sb.append(":").append(meta);
        }
        return sb.toString();
    }

}
