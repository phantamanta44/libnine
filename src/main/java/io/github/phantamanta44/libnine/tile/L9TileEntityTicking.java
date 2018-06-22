package io.github.phantamanta44.libnine.tile;

import net.minecraft.util.ITickable;

public abstract class L9TileEntityTicking extends L9TileEntity implements ITickable {

    private boolean shouldDispatchUpdate;
    private boolean initialized;

    public L9TileEntityTicking() {
        this.shouldDispatchUpdate = false;
        this.initialized = false;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized() {
        initialized = true;
    }

    @Override
    protected void setDirty() {
        markDirty();
        shouldDispatchUpdate = true;
    }

    @Override
    public void update() {
        if (shouldDispatchUpdate) {
            dispatchTileUpdate();
            shouldDispatchUpdate = false;
        }
        if (isInitialized()) tick();
    }

    protected abstract void tick();

}
