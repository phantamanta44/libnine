package xyz.phanta.libnine.network

import net.minecraftforge.fml.network.NetworkEvent
import xyz.phanta.libnine.container.NineContainer
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter

object PacketClientContainerInteraction : PacketType<PacketClientContainerInteraction.Packet> {

    class Packet(val data: ByteArray) : PacketData

    override val type: Class<Packet>
        get() = Packet::class.java

    override fun serialize(stream: ByteWriter, data: Packet) {
        stream.varPrecision(data.data.size).bytes(data.data)
    }

    override fun deserialize(stream: ByteReader): Packet = Packet(stream.bytes(stream.varPrecision()))

    override fun handle(packet: Packet, context: NetworkEvent.Context) {
        context.enqueueWork {
            context.sender!!.openContainer?.let { (it as NineContainer).onClientInteraction(ByteReader(packet.data)) }
        }
    }

}
