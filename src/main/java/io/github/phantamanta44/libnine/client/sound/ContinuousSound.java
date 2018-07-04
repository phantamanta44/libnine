package io.github.phantamanta44.libnine.client.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nullable;

public class ContinuousSound implements ITickableSound {

    private final ResourceLocation resource;
    private final float volumeFactor;
    private final float pitchFactor;
    private final float x, y, z;
    private final SoundCategory category;
    private final int initialTtl;
    private int ttl;
    private int vol;
    private SoundEventAccessor soundEvent;
    private Sound sound;

    public ContinuousSound(ResourceLocation resource, float volume, float pitch, float x, float y, float z,
                           SoundCategory category, int ttl) {
        this.resource = resource;
        this.volumeFactor = volume / 100F;
        this.pitchFactor = (pitch - 0.5F) / 100F;
        this.x = x;
        this.y = y;
        this.z = z;
        this.category = category;
        this.ttl = this.initialTtl = ttl;
        this.vol = 1;
    }

    public ContinuousSound(ResourceLocation resource, float volume, float pitch, Vec3i pos, SoundCategory category, int ttl) {
        this(resource, volume, pitch, pos.getX(), pos.getY(), pos.getZ(), category, ttl);
    }

    public ContinuousSound(ResourceLocation resource, float volume, float pitch, SoundCategory category, int ttl) {
        this(resource, volume, pitch, 0F, 0F, 0F, category, ttl);
    }

    public ContinuousSound(ResourceLocation resource, float volume, float pitch, int ttl) {
        this(resource, volume, pitch, SoundCategory.MASTER, ttl);
    }

    public ContinuousSound(ResourceLocation resource, int ttl) {
        this(resource, 1F, 1F, ttl);
    }

    @Override
    public boolean isDonePlaying() {
        return ttl <= 0 && vol <= 0;
    }

    @Override
    public ResourceLocation getSoundLocation() {
        return resource;
    }

    @Nullable
    @Override
    public SoundEventAccessor createAccessor(SoundHandler handler) {
        soundEvent = handler.getAccessor(resource);
        sound = soundEvent != null ? soundEvent.cloneEntry() : SoundHandler.MISSING_SOUND;
        return soundEvent;
    }

    @Override
    public Sound getSound() {
        return sound;
    }

    @Override
    public SoundCategory getCategory() {
        return category;
    }

    @Override
    public boolean canRepeat() {
        return true;
    }

    @Override
    public int getRepeatDelay() {
        return 0;
    }

    @Override
    public float getVolume() {
        return vol * volumeFactor;
    }

    @Override
    public float getPitch() {
        return vol * pitchFactor + 0.5F;
    }

    @Override
    public float getXPosF() {
        return x;
    }

    @Override
    public float getYPosF() {
        return y;
    }

    @Override
    public float getZPosF() {
        return z;
    }

    @Override
    public AttenuationType getAttenuationType() {
        return AttenuationType.LINEAR;
    }

    @Override
    public void update() {
        if (ttl > 0) {
            if (vol < 100)
                vol = Math.min(vol + 5, 100);
            ttl--;
        } else if (vol > 0) {
            vol = Math.max((int)Math.floor(vol - 7), 0);
        }
    }

    public void refresh() {
        ttl = initialTtl;
        vol = Math.max(vol, 1);
    }

}
