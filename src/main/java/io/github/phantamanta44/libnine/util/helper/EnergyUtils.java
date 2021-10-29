package io.github.phantamanta44.libnine.util.helper;

import net.minecraftforge.energy.IEnergyStorage;

import java.util.*;

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

    public static IEnergyStorage restrict(IEnergyStorage storage, int insertLimit, int extractLimit) {
        return new RatedEnergyStorage(storage, insertLimit, extractLimit);
    }

    public static IEnergyStorage join(IEnergyStorage... storages) {
        return join(Arrays.asList(storages));
    }

    public static IEnergyStorage join(List<? extends IEnergyStorage> storages) {
        return new CascadingEnergyStorage(storages);
    }

    private static class RatedEnergyStorage implements IEnergyStorage {

        private final IEnergyStorage delegate;
        private final int insertLimit, extractLimit;

        public RatedEnergyStorage(IEnergyStorage delegate, int insertLimit, int extractLimit) {
            this.delegate = delegate;
            this.insertLimit = insertLimit;
            this.extractLimit = extractLimit;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return delegate.receiveEnergy(Math.min(maxReceive, insertLimit), simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return delegate.extractEnergy(Math.min(maxExtract, extractLimit), simulate);
        }

        @Override
        public int getEnergyStored() {
            return delegate.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return delegate.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return extractLimit > 0 && delegate.canExtract();
        }

        @Override
        public boolean canReceive() {
            return insertLimit > 0 && delegate.canReceive();
        }

    }

    private static class CascadingEnergyStorage implements IEnergyStorage {

        private final List<? extends IEnergyStorage> delegates;

        public CascadingEnergyStorage(List<? extends IEnergyStorage> delegates) {
            this.delegates = delegates;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int remaining = maxReceive;
            for (IEnergyStorage delegate : delegates) {
                remaining -= delegate.receiveEnergy(remaining, simulate);
                if (remaining <= 0) {
                    return maxReceive;
                }
            }
            return Math.min(maxReceive - remaining, maxReceive);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int remaining = maxExtract;
            for (IEnergyStorage delegate : delegates) {
                remaining -= delegate.extractEnergy(remaining, simulate);
                if (remaining <= 0) {
                    return maxExtract;
                }
            }
            return Math.min(maxExtract - remaining, maxExtract);
        }

        @Override
        public int getEnergyStored() {
            return delegates.stream().mapToInt(IEnergyStorage::getEnergyStored).sum();
        }

        @Override
        public int getMaxEnergyStored() {
            return delegates.stream().mapToInt(IEnergyStorage::getMaxEnergyStored).sum();
        }

        @Override
        public boolean canExtract() {
            return delegates.stream().anyMatch(IEnergyStorage::canExtract);
        }

        @Override
        public boolean canReceive() {
            return delegates.stream().anyMatch(IEnergyStorage::canReceive);
        }

    }

}
