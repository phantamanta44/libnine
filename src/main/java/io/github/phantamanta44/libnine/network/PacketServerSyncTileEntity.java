package io.github.phantamanta44.libnine.network;

import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.tile.L9TileEntity;
import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

@SuppressWarnings("NotNullFieldNotInitialized")
public class PacketServerSyncTileEntity implements IMessage {

    private int dimension;
    private BlockPos pos;
    private byte[] data;

    public PacketServerSyncTileEntity() {
        // NO-OP
    }

    public PacketServerSyncTileEntity(L9TileEntity tile) {
        this.dimension = tile.getWorld().provider.getDimension();
        this.pos = tile.getPos();
        ByteUtils.Writer writer = ByteUtils.writer();
        tile.serBytes(writer);
        this.data = writer.toArray();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.dimension = buf.readInt();
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        pos = new BlockPos(x, y, z);
        data = new byte[buf.readableBytes()];
        buf.readBytes(data);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimension);
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeBytes(data);
    }

    public static class Handler implements IMessageHandler<PacketServerSyncTileEntity, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(PacketServerSyncTileEntity message, MessageContext ctx) {
            World world = LibNine.PROXY.getDimensionWorld(message.dimension);
            if (world != null) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    TileEntity tile = world.getTileEntity(message.pos);
                    if (tile instanceof L9TileEntity) {
                        ((L9TileEntity)tile).onTileSyncPacket(ByteUtils.reader(message.data));
                    } else {
                        LibNine.LOGGER.warn("No tile exists for sync packet at pos {}", message.pos);
                    }
                });
            }
            return null;
        }

    }

}
