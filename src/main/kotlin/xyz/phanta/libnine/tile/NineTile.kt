package xyz.phanta.libnine.tile

import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.Direction
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

    protected val daedalus: Daedalus = Daedalus()
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
                    PacketDistributor.NEAR.with { world!!.getPacketRange(pos, 64.0) },
                    PacketServerSyncTileEntity.Packet(pos, ByteWriter().also { daedalus.genDeltaByteStream(it) }.toArray())
            )
        }
    }

    // behaviour

    override fun <T> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> =
            capabilities?.getCapability(cap, side) ?: LazyOptional.empty()

    override fun remove() {
        super.remove()
        daedalus.destroy()
    }

    // serialization

    override fun serNbt(tag: CompoundNBT) {
        tag.put("data", daedalus.genFullTag())
    }

    override fun deserNbt(tag: CompoundNBT) = daedalus.readTag(tag.getCompound("data"))

    override fun serByteStream(stream: ByteWriter) = daedalus.genFullByteStream(stream)

    override fun deserByteStream(stream: ByteReader) = daedalus.readByteStream(stream)

    override fun read(compound: CompoundNBT) {
        super.read(compound)
        deserNbt(compound)
    }

    override fun write(compound: CompoundNBT): CompoundNBT = super.write(compound).also { serNbt(it) }

    open fun onTileSyncPacket(stream: ByteReader) {
        deserByteStream(stream)
    }

}

abstract class NineTileTicking(mod: Virtue, type: TileEntityType<*>, requiresSync: Boolean)
    : NineTile(mod, type, requiresSync), ITickableTileEntity {

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
