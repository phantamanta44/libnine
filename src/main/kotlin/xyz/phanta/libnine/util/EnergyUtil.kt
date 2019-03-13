package xyz.phanta.libnine.util

import net.minecraft.tileentity.TileEntity
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.energy.IEnergyStorage
import xyz.phanta.libnine.util.world.adjacentCaps

object EnergyUtil {

    fun distribute(amount: Int, receivers: List<IEnergyStorage>): Int {
        var remainingEnergy = amount
        var remainingReceivers = receivers
        while (remainingEnergy > 0 && remainingReceivers.isNotEmpty()) {
            val initialEnergy = remainingEnergy
            val portion = remainingEnergy / remainingReceivers.size
            remainingReceivers = remainingReceivers.filter {
                if (!it.canReceive()) {
                    false
                } else {
                    val transferred = it.receiveEnergy(portion, false)
                    remainingEnergy -= transferred
                    transferred == portion
                }
            }
            if (remainingEnergy == initialEnergy) break
        }
        return amount - remainingEnergy
    }

    fun distributeAdjacent(tile: TileEntity, amount: Int): Int =
            distribute(amount, tile.adjacentCaps(CapabilityEnergy.ENERGY))

}
