package io.github.phantamanta44.libnine.util.world;

import io.github.phantamanta44.libnine.constant.NameConst;
import io.github.phantamanta44.libnine.util.ImpossibilityRealizedException;
import io.github.phantamanta44.libnine.util.format.ILocalizable;

import java.util.function.Predicate;

public enum RedstoneBehaviour implements ILocalizable {

    IGNORED(p -> true),
    DIRECT(p -> p.getWorld().isBlockPowered(p.getPos())),
    INVERTED(p -> !p.getWorld().isBlockPowered(p.getPos()));

    private final Predicate<WorldBlockPos> pred;

    RedstoneBehaviour(Predicate<WorldBlockPos> pred) {
        this.pred = pred;
    }

    public boolean canWork(WorldBlockPos pos) {
        return pred.test(pos);
    }

    @Override
    public String getTranslationKey() {
        switch (this) {
            case IGNORED:
                return NameConst.INFO_REDSTONE_BEHAVIOUR_IGNORED;
            case DIRECT:
                return NameConst.INFO_REDSTONE_BEHAVIOUR_DIRECT;
            case INVERTED:
                return NameConst.INFO_REDSTONE_BEHAVIOUR_INVERTED;
        }
        throw new ImpossibilityRealizedException();
    }

}
