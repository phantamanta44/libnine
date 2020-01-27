package xyz.phanta.libnine.network

import net.minecraft.network.PacketBuffer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.network.NetworkEvent
import net.minecraftforge.fml.network.NetworkRegistry
import net.minecraftforge.fml.network.PacketDistributor
import net.minecraftforge.fml.network.simple.SimpleChannel
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import java.util.function.BiConsumer
import java.util.function.Supplier

private const val PROTOCOL_VERSION: String = "0"

class NetworkHandler(channelId: ResourceLocation) {

    private val channel: SimpleChannel = NetworkRegistry.newSimpleChannel(
            channelId, { PROTOCOL_VERSION }, { it == PROTOCOL_VERSION }, { it == PROTOCOL_VERSION })

    fun postToClient(target: PacketDistributor.PacketTarget, message: PacketData) {
        channel.send(target, message)
    }

    fun postToServer(message: PacketData) {
        channel.sendToServer(message)
    }

    @Suppress("INACCESSIBLE_TYPE")
    fun <T : PacketData> registerType(id: Int, type: PacketType<T>) {
        channel.registerMessage(id, type.type, Serializer(type), Deserializer(type), Handler(type))
    }

    private class Serializer<T : PacketData>(private val type: PacketType<T>) : BiConsumer<T, PacketBuffer> {

        override fun accept(packet: T, buf: PacketBuffer) {
            buf.writeByteArray(ByteWriter().also { type.serialize(it, packet) }.toArray())
        }

    }

    private class Deserializer<T : PacketData>(private val type: PacketType<T>) : java.util.function.Function<PacketBuffer, T> {

        override fun apply(buf: PacketBuffer): T = type.deserialize(ByteReader(buf.readByteArray()))

    }

    private class Handler<T : PacketData>(private val type: PacketType<T>) : BiConsumer<T, Supplier<NetworkEvent.Context>> {

        override fun accept(packet: T, context: Supplier<NetworkEvent.Context>) {
            context.get().let {
                type.handle(packet, it)
                it.packetHandled = true
            }
        }

    }

}

interface PacketType<T : PacketData> {

    val type: Class<T>

    fun serialize(stream: ByteWriter, data: T)

    fun deserialize(stream: ByteReader): T

    fun handle(packet: T, context: NetworkEvent.Context)

}

interface PacketData
