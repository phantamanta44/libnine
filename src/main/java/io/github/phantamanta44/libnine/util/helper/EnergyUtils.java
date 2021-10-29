package io.github.phantamanta44.libnine.util.helper;

import net.minecraftforge.energy.IEnergyStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class EnergyUtils {

    public static int distribute(int amount, Collection<IEnergyStorage> receivers) {
        int remainingEnergy = amount;
        Set<IEnergyStorage> remainingReceivers = new HashSet<>(receivers);
        while (remainingEnergy > 0 && !remainingReceivers.isEmpty()) {
            int energyAtStartOfIteration = remainingEnergy;
            int portionPerReceiver = remainingEnergy / remainingReceivers.size();
            Iterator<IEnergyStorage> iter = remainingReceivers.iterator();
            while (iter.hasNext()) {
                IEnergyStorage receiver = iter.next();
                if (!receiver.canReceive()) {
                    iter.remove();
                    continue;
                }
                int transferred = receiver.receiveEnergy(portionPerReceiver, false);
                remainingEnergy -= transferred;
                if (transferred >= portionPerReceiver) {
                    iter.remove();
                }
            }
            if (remainingEnergy >= energyAtStartOfIteration) {
                break;
            }
        }
        return Math.min(amount - remainingEnergy, amount);
    }

}
