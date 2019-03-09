package xyz.phanta.libnine.network

import net.minecraft.client.Minecraft
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.network.NetworkEvent
import xyz.phanta.libnine.recipe.Recipe
import xyz.phanta.libnine.recipe.RecipeSet
import xyz.phanta.libnine.recipe.RecipeType
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

    override fun handle(packet: Packet, context: () -> NetworkEvent.Context) {
        context().enqueueWork<Nothing?> {
            ((Minecraft.getInstance().world.getTileEntity(packet.pos)
                    ?: throw IllegalStateException("Expected tile entity at ${packet.pos}!"))
                    as? NineTile ?: throw IllegalStateException("Expected libnine tile entity at ${packet.pos}!"))
                    .deserByteStream(ByteReader(packet.data))
        }
    }

}

object PacketServerSyncRecipeSet : PacketType<PacketServerSyncRecipeSet.Packet> {

    class Packet(val type: RecipeType<*, *, *>, val recipes: List<Recipe<*, *>>) : PacketData

    override val type: Class<Packet>
        get() = Packet::class.java

    override fun serialize(stream: ByteWriter, data: Packet) {
        stream.resourceLocation(data.type.name)
        serializeList<Any, Any, Recipe<Any, Any>>(stream, data)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <I, O, R : Recipe<I, O>> serializeList(stream: ByteWriter, data: Packet) {
        stream.varPrecision(data.recipes.size)
        data.recipes.forEach { (data.type as RecipeType<I, O, R>).serializer(stream, it as R) }
    }

    override fun deserialize(stream: ByteReader): Packet {
        val type = RecipeSet.typeForName<Any, Any, Recipe<Any, Any>>(stream.resourceLocation())
        val list = mutableListOf<Recipe<Any, Any>>()
        deserializeList(type, stream, list)
        return Packet(type, list)
    }

    private fun <I, O, R : Recipe<I, O>> deserializeList(type: RecipeType<I, O, R>, stream: ByteReader, list: MutableList<Recipe<I, O>>) {
        for (i in 0 until stream.varPrecision()) list += type.deserializer(stream)
    }

    @Suppress("UNCHECKED_CAST")
    override fun handle(packet: Packet, context: () -> NetworkEvent.Context) {
        (RecipeSet.setForType(packet.type).recipes as MutableList<Recipe<Any, Any>>)
                .addAll(packet.recipes as List<Recipe<Any, Any>>)
    }

}
