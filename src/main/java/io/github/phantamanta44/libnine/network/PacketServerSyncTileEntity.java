package io.github.phantamanta44.libnine.network;

import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.tile.L9TileEntity;
import io.github.phantamanta44.libnine.util.world.WorldBlockPos;
import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketServerSyncTileEntity implements IMessage {

    private WorldBlockPos pos;
    private byte[] data;

    public PacketServerSyncTileEntity() {
        // NO-OP
    }

    public PacketServerSyncTileEntity(L9TileEntity tile) {
        this.pos = new WorldBlockPos(tile.getWorld(), tile.getPos());
        ByteUtils.Writer writer = ByteUtils.writer();
        tile.serBytes(writer);
        this.data = writer.toArray();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int dim = buf.readInt();
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        pos = new WorldBlockPos(dim, x, y, z);
        data = new byte[buf.readableBytes()];
        buf.readBytes(data);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getWorld().provider.getDimension());
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeBytes(data);
    }

    public static class Handler implements IMessageHandler<PacketServerSyncTileEntity, IMessage> {

        @Override
        public IMessage onMessage(PacketServerSyncTileEntity message, MessageContext ctx) {
            if (message.pos.getWorld().provider.getDimension()
                    == Minecraft.getMinecraft().world.provider.getDimension()) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    TileEntity tile = Minecraft.getMinecraft().world.getTileEntity(message.pos.getPos());
                    if (tile instanceof L9TileEntity) {
                        ((L9TileEntity)tile).deserBytes(ByteUtils.reader(message.data));
                    } else {
                        LibNine.LOGGER.warn("No tile exists for sync packet at pos {}", message.pos);
                    }
                });
            }
            return null;
        }

    }

}
