package xyz.phanta.libnine.capability.impl

import net.minecraftforge.energy.IEnergyStorage
import xyz.phanta.libnine.util.component.reservoir.IntReservoir
import xyz.phanta.libnine.util.data.Serializable

class AspectEnergy(private val reservoir: IntReservoir) : IEnergyStorage, Serializable by reservoir {

    override fun getEnergyStored(): Int = reservoir.quantity

    override fun getMaxEnergyStored(): Int = reservoir.capacity

    override fun canExtract(): Boolean = true

    override fun extractEnergy(maxExtract: Int, simulate: Boolean): Int = reservoir.draw(maxExtract, !simulate)

    override fun canReceive(): Boolean = true

    override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int = reservoir.offer(maxReceive, !simulate)

}
