package io.github.phantamanta44.libnine.client.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public class RepeatingSound extends SingleSound implements ITickableSound {

    private boolean repeating = true;

    public RepeatingSound(ResourceLocation resource, float volume, float pitch, SoundCategory category) {
        super(resource, volume, pitch, category);
    }

    @Override
    public boolean canRepeat() {
        return repeating;
    }

    @Override
    public boolean isDonePlaying() {
        return !repeating;
    }

    @Override
    public void update() {
        // NO-OP
    }

    public void kill() {
        repeating = false;
    }

}
