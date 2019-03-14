package xyz.phanta.libnine.tile

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fml.network.PacketDistributor
import xyz.phanta.libnine.Virtue
import xyz.phanta.libnine.network.PacketServerSyncTileEntity
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.Serializable
import xyz.phanta.libnine.util.data.daedalus.Daedalus
import xyz.phanta.libnine.util.world.getPacketRange

abstract class NineTile(
        private val mod: Virtue,
        type: TileEntityType<*>,
        private val requiresSync: Boolean
) : TileEntity(type), Serializable {

    @Suppress("LeakingThis")
    protected val serializer: Daedalus<*> = Daedalus(this)
    protected val capabilities: ICapabilityProvider? by lazy { initCapabilities() }

    // init

    protected open fun initCapabilities(): ICapabilityProvider? = null

    // api

    protected open fun dirty() {
        markDirty()
        if (!getWorld()!!.isRemote) dispatchTileUpdate()
    }

    protected fun dispatchTileUpdate() {
        if (requiresSync) {
            mod.netHandler.postToClient(
                    PacketDistributor.NEAR.with { world.getPacketRange(pos, 64.0) },
                    PacketServerSyncTileEntity.Packet(pos, ByteWriter().also { serByteStream(it) }.toArray())
            )
        }
    }

    // behaviour

    override fun <T> getCapability(cap: Capability<T>, side: EnumFacing?): LazyOptional<T> =
            capabilities?.getCapability(cap, side) ?: LazyOptional.empty()

    // serialization

    override fun serNbt(tag: NBTTagCompound) = serializer.serNbt(tag)

    override fun deserNbt(tag: NBTTagCompound) = serializer.deserNbt(tag)

    override fun serByteStream(stream: ByteWriter) = serializer.serByteStream(stream)

    override fun deserByteStream(stream: ByteReader) = serializer.deserByteStream(stream)

    override fun read(compound: NBTTagCompound) {
        super.read(compound)
        deserNbt(compound)
    }

    override fun write(compound: NBTTagCompound): NBTTagCompound = super.write(compound).also { serNbt(it) }

}

abstract class NineTileTicking(mod: Virtue, type: TileEntityType<*>, requiresSync: Boolean)
    : NineTile(mod, type, requiresSync), ITickable {

    private var dirty: Boolean = false

    override fun dirty() {
        markDirty()
        dirty = true
    }

    override fun tick() {
        if (dirty) {
            dispatchTileUpdate()
            dirty = false
        }
        doTick()
    }

    abstract fun doTick()

}
