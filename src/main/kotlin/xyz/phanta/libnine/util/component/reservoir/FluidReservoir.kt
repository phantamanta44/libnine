package xyz.phanta.libnine.util.component.reservoir

import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.CompoundNBT
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.registries.ForgeRegistries
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.daedalus.IncrementalDataListener
import xyz.phanta.libnine.util.data.daedalus.IncrementalSerializable
import xyz.phanta.libnine.util.data.nbt.deserializeResourceLocation
import xyz.phanta.libnine.util.data.nbt.serializeNbt

interface FluidReservoir : IntReservoir, IFluidHandler

class TypedFluidReservoir(private val backing: IntReservoir, private val fluidType: Fluid)
    : IntReservoir by backing, IFluidHandler {

    override fun drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack =
            if (resource.isEmpty || !resource.fluid.isEquivalentTo(fluidType)) {
                FluidStack.EMPTY
            } else {
                drain(resource.amount, action)
            }

    override fun drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack =
            FluidStack(fluidType, draw(maxDrain, action.execute()))

    override fun getTankCapacity(tank: Int): Int = if (tank == 0) capacity else 0

    override fun fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int =
            if (resource.isEmpty || !resource.fluid.isEquivalentTo(fluidType)) {
                0
            } else {
                offer(resource.amount, action.execute())
            }

    override fun getFluidInTank(tank: Int): FluidStack =
            if (tank != 0 || isEmpty()) FluidStack.EMPTY else FluidStack(fluidType, quantity)

    override fun getTanks(): Int = 1

    override fun isFluidValid(tank: Int, stack: FluidStack): Boolean = tank == 0 && stack.fluid.isEquivalentTo(fluidType)

}

class UntypedFluidReservoir(private val backing: IntReservoir)
    : IncrementalSerializable(), IntReservoir by backing, IFluidHandler {

    private var fluidType: Fluid = Fluids.EMPTY

    override fun drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack =
            if (resource.isEmpty || !checkFluid(fluidType)) {
                FluidStack.EMPTY
            } else {
                drain(resource.amount, action)
            }

    override fun drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack =
            if (action.execute()) {
                val result = FluidStack(fluidType, draw(maxDrain, true))
                if (isEmpty()) {
                    fluidType = Fluids.EMPTY
                }
                result
            } else {
                FluidStack(fluidType, draw(maxDrain, false))
            }

    override fun getTankCapacity(tank: Int): Int = if (tank == 0) capacity else 0

    override fun fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int =
            if (resource.isEmpty || !checkFluid(resource.fluid)) {
                0
            } else {
                fluidType = resource.fluid
                offer(resource.amount, action.execute())
            }

    override fun getFluidInTank(tank: Int): FluidStack =
            if (tank != 0 || isEmpty()) FluidStack.EMPTY else FluidStack(fluidType, quantity)

    override fun getTanks(): Int = 1

    override fun isFluidValid(tank: Int, stack: FluidStack): Boolean = tank == 0 && checkFluid(stack.fluid)

    private fun checkFluid(fluid: Fluid) = isEmpty() || fluid.isEquivalentTo(fluidType)

    override fun extractListener(): IncrementalDataListener = super.extractListener()

    override fun serNbt(tag: CompoundNBT) {
        backing.serNbt(tag)
        tag.put("fluid", fluidType.registryName!!.serializeNbt())
    }

    override fun deserNbt(tag: CompoundNBT) {
        backing.deserNbt(tag)
        fluidType = ForgeRegistries.FLUIDS.getValue(tag.getCompound("fluid").deserializeResourceLocation())!!
    }

    override fun serByteStream(stream: ByteWriter) {
        backing.serByteStream(stream)
        stream.resourceLocation(fluidType.registryName!!)
    }

    override fun deserByteStream(stream: ByteReader) {
        backing.deserByteStream(stream)
        fluidType = ForgeRegistries.FLUIDS.getValue(stream.resourceLocation())!!
    }

}
