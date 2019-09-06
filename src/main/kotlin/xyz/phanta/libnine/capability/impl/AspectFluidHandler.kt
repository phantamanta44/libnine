package xyz.phanta.libnine.capability.impl

import net.minecraft.nbt.CompoundNBT
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import xyz.phanta.libnine.util.component.reservoir.FluidReservoir
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.daedalus.ByteStreamDeltaMarker
import xyz.phanta.libnine.util.data.daedalus.IncrementalComposition
import xyz.phanta.libnine.util.data.nbt.asNbtList

open class AspectFluidHandler(private vararg val tanks: FluidReservoir)
    : IncrementalComposition<IncrementalComposition.Listener>(*tanks), IFluidHandler {

    private val deltaMarker: ByteStreamDeltaMarker = ByteStreamDeltaMarker { tanks.size }

    override fun drain(resource: FluidStack?, action: IFluidHandler.FluidAction?): FluidStack = tanks.asSequence()
            .map { it.drain(resource, action) }.firstOrNull { !it.isEmpty } ?: FluidStack.EMPTY

    override fun drain(maxDrain: Int, action: IFluidHandler.FluidAction?): FluidStack = tanks.asSequence()
            .map { it.drain(maxDrain, action) }.firstOrNull { !it.isEmpty } ?: FluidStack.EMPTY

    override fun fill(resource: FluidStack?, action: IFluidHandler.FluidAction?): Int = tanks.asSequence()
            .map { it.fill(resource, action) }.firstOrNull { it > 0 } ?: 0

    override fun getTanks(): Int = tanks.size

    override fun getFluidInTank(tank: Int): FluidStack = tanks[tank].getFluidInTank(0)

    override fun getTankCapacity(tank: Int): Int = tanks[tank].getTankCapacity(0)

    override fun isFluidValid(tank: Int, stack: FluidStack): Boolean = tanks[tank].isFluidValid(0, stack)

    override fun serNbt(tag: CompoundNBT) {
        tag.put("tanks", tanks.asNbtList { it.createSerializedNbt() })
    }

    override fun deserNbt(tag: CompoundNBT) {
        val list = tag.getList("tanks", Constants.NBT.TAG_COMPOUND)
        for (i in tanks.indices) {
            val tankTag = list.getCompound(i)
            if (!tankTag.isEmpty) {
                tanks[i].deserNbt(tankTag)
            }
        }
    }

    override fun serByteStream(stream: ByteWriter) {
        deltaMarker.writeFullField(stream)
        tanks.forEach { it.serByteStream(stream) }
    }

    override fun deserByteStream(stream: ByteReader) {
        val field = deltaMarker.readField(stream)
        for (i in tanks.indices) {
            if (field[i]) {
                tanks[i].deserByteStream(stream)
            }
        }
    }

    override fun createListener(): Listener = Listener(this)

}
