package io.github.phantamanta44.libnine.util.world;

import com.google.common.collect.ImmutableList;
import io.github.phantamanta44.libnine.constant.NameConst;
import io.github.phantamanta44.libnine.util.format.ILocalizable;

public enum RedstoneBehaviour implements ILocalizable {

    IGNORED {
        @Override
        public boolean canWork(boolean powered) {
            return true;
        }

        @Override
        public boolean canWork(WorldBlockPos pos) {
            return true;
        }

        @Override
        public String getTranslationKey() {
            return NameConst.INFO_REDSTONE_BEHAVIOUR_IGNORED;
        }
    },
    DIRECT {
        @Override
        public boolean canWork(boolean powered) {
            return powered;
        }

        @Override
        public boolean canWork(WorldBlockPos pos) {
            return pos.getWorld().isBlockPowered(pos.getPos());
        }

        @Override
        public String getTranslationKey() {
            return NameConst.INFO_REDSTONE_BEHAVIOUR_DIRECT;
        }
    },
    INVERTED {
        @Override
        public boolean canWork(boolean powered) {
            return !powered;
        }

        @Override
        public boolean canWork(WorldBlockPos pos) {
            return !pos.getWorld().isBlockPowered(pos.getPos());
        }

        @Override
        public String getTranslationKey() {
            return NameConst.INFO_REDSTONE_BEHAVIOUR_INVERTED;
        }
    };

    public static final ImmutableList<RedstoneBehaviour> VALUES = ImmutableList.copyOf(values());

    public abstract boolean canWork(boolean powered);

    public abstract boolean canWork(WorldBlockPos pos);

    @Override
    public abstract String getTranslationKey();

}
