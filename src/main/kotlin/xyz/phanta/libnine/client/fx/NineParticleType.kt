package xyz.phanta.libnine.client.fx

import com.mojang.brigadier.StringReader
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.IParticleFactory
import net.minecraft.client.particle.Particle
import net.minecraft.network.PacketBuffer
import net.minecraft.particles.IParticleData
import net.minecraft.particles.ParticleType
import net.minecraft.util.ResourceLocation
import net.minecraft.util.registry.IRegistry
import net.minecraft.world.World
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter

abstract class NineParticleType<X>(private val name: ResourceLocation, private val force: Boolean) {

    @Suppress("LeakingThis")
    internal val type: ParticleType<Data<X>> = object : ParticleType<Data<X>>(name, force, Deserializer(this)) {}

    internal val factory: IParticleFactory<Data<X>> = IParticleFactory { context, world, x, y, z, velX, velY, velZ ->
        createParticle(context.data, world, x, y, z, velX, velY, velZ)
    }

    internal class Data<X>(private val type: NineParticleType<X>, internal val data: X) : IParticleData {

        override fun getParameters(): String = type.name.toString()

        override fun getType(): ParticleType<*> = type.type

        override fun write(buf: PacketBuffer) {
            val bytes = ByteWriter().also { type.serialize(it, data) }.toArray()
            buf.writeByteArray(bytes)
        }

    }

    private class Deserializer<X>(private val nineType: NineParticleType<X>) : IParticleData.IDeserializer<Data<X>> {

        override fun deserialize(type: ParticleType<Data<X>>, cmd: StringReader): Data<X> =
                Data(nineType, nineType.deserialize(cmd))

        override fun read(type: ParticleType<Data<X>>, buf: PacketBuffer): Data<X> =
                Data(nineType, nineType.deserialize(ByteReader(buf.readByteArray())))

    }

    internal fun register() {
        IRegistry.field_212632_u.put(name, type)
        Minecraft.getInstance().particles.registerFactory(type, factory)
    }

    abstract fun serialize(stream: ByteWriter, data: X)

    abstract fun deserialize(stream: ByteReader): X

    abstract fun deserialize(cmd: StringReader): X

    abstract fun createParticle(
            context: X,
            world: World,
            x: Double,
            y: Double,
            z: Double,
            velX: Double,
            velY: Double,
            velZ: Double
    ): Particle

}

abstract class NineSimpleParticleType(name: ResourceLocation, force: Boolean) : NineParticleType<Unit>(name, force) {

    override fun serialize(stream: ByteWriter, data: Unit) {
        // NO-OP
    }

    override fun deserialize(stream: ByteReader) {
        // NO-OP
    }

    override fun deserialize(cmd: StringReader) {
        // NO-OP
    }

    override fun createParticle(
            context: Unit,
            world: World,
            x: Double,
            y: Double,
            z: Double,
            velX: Double,
            velY: Double,
            velZ: Double
    ): Particle = createParticle(world, x, y, z, velX, velY, velZ)

    abstract fun createParticle(world: World, x: Double, y: Double, z: Double, velX: Double, velY: Double, velZ: Double): Particle

}
