package io.github.phantamanta44.libnine.util.world;

import java.util.function.Predicate;

public enum RedstoneBehaviour {

    IGNORED(p -> true),
    DIRECT(p -> p.getWorld().isBlockIndirectlyGettingPowered(p.getPos()) > 0),
    INVERTED(p -> p.getWorld().isBlockIndirectlyGettingPowered(p.getPos()) == 0);

    private final Predicate<WorldBlockPos> pred;

    RedstoneBehaviour(Predicate<WorldBlockPos> pred) {
        this.pred = pred;
    }

    public boolean canWork(WorldBlockPos pos) {
        return pred.test(pos);
    }

}
