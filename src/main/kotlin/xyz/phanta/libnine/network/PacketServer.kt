package xyz.phanta.libnine.network

import net.minecraft.client.Minecraft
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.network.NetworkEvent
import xyz.phanta.libnine.container.NineContainer
import xyz.phanta.libnine.tile.NineTile
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter

object PacketServerSyncTileEntity : PacketType<PacketServerSyncTileEntity.Packet> {

    class Packet(val pos: BlockPos, val data: ByteArray) : PacketData

    override val type: Class<Packet>
        get() = Packet::class.java

    override fun serialize(stream: ByteWriter, data: Packet) {
        stream.blockPos(data.pos).int(data.data.size).bytes(data.data)
    }

    override fun deserialize(stream: ByteReader): Packet = Packet(stream.blockPos(), stream.bytes(stream.int()))

    override fun handle(packet: Packet, context: () -> NetworkEvent.Context) {
        context().enqueueWork<Nothing?> {
            ((Minecraft.getInstance().world.getTileEntity(packet.pos)
                    ?: throw IllegalStateException("Expected tile entity at ${packet.pos}!"))
                    as? NineTile ?: throw IllegalStateException("Expected libnine tile entity at ${packet.pos}!"))
                    .deserByteStream(ByteReader(packet.data))
        }
    }

}

object PacketClientContainerInteraction : PacketType<PacketClientContainerInteraction.Packet> {

    class Packet(val data: ByteArray) : PacketData

    override val type: Class<Packet>
        get() = Packet::class.java

    override fun serialize(stream: ByteWriter, data: Packet) {
        stream.int(data.data.size).bytes(data.data)
    }

    override fun deserialize(stream: ByteReader): Packet = Packet(stream.bytes(stream.int()))

    override fun handle(packet: Packet, context: () -> NetworkEvent.Context) {
        context().let { ctx ->
            ctx.enqueueWork<Nothing?> {
                ctx.sender!!.openContainer?.let { (it as NineContainer).onClientInteraction(ByteReader(packet.data)) }
            }
        }
    }

}
