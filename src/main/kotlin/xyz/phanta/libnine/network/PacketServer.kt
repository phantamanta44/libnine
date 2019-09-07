package xyz.phanta.libnine.network

import net.minecraft.client.Minecraft
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.network.NetworkEvent
import xyz.phanta.libnine.tile.NineTile
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter

object PacketServerSyncTileEntity : PacketType<PacketServerSyncTileEntity.Packet> {

    class Packet(val pos: BlockPos, val data: ByteArray) : PacketData

    override val type: Class<Packet>
        get() = Packet::class.java

    override fun serialize(stream: ByteWriter, data: Packet) {
        stream.blockPos(data.pos).varPrecision(data.data.size).bytes(data.data)
    }

    override fun deserialize(stream: ByteReader): Packet = Packet(stream.blockPos(), stream.bytes(stream.varPrecision()))

    override fun handle(packet: Packet, context: NetworkEvent.Context) {
        context.enqueueWork {
            ((Minecraft.getInstance().world.getTileEntity(packet.pos)
                    ?: throw IllegalStateException("Expected tile entity at ${packet.pos}!"))
                    as? NineTile ?: throw IllegalStateException("Expected libnine tile entity at ${packet.pos}!"))
                    .onTileSyncPacket(ByteReader(packet.data))
        }
    }

}
