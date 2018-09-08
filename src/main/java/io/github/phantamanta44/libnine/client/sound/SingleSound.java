package io.github.phantamanta44.libnine.client.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nullable;

public class SingleSound implements ISound {

    private final ResourceLocation resource;
    private final float vol;
    private final float pitch;
    private final float x, y, z;
    private final SoundCategory category;
    @SuppressWarnings("NullableProblems")
    private Sound sound;

    public SingleSound(ResourceLocation resource, float volume, float pitch, float x, float y, float z, SoundCategory category) {
        this.resource = resource;
        this.vol = volume;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.category = category;
    }

    public SingleSound(ResourceLocation resource, float volume, float pitch, Vec3i pos, SoundCategory category) {
        this(resource, volume, pitch, pos.getX(), pos.getY(), pos.getZ(), category);
    }

    public SingleSound(ResourceLocation resource, float volume, float pitch, SoundCategory category) {
        this(resource, volume, pitch, 0F, 0F, 0F, category);
    }

    public SingleSound(ResourceLocation resource, float volume, float pitch) {
        this(resource, volume, pitch, SoundCategory.MASTER);
    }

    public SingleSound(ResourceLocation resource) {
        this(resource, 1F, 1F);
    }

    @Override
    public ResourceLocation getSoundLocation() {
        return resource;
    }

    @Nullable
    @Override
    public SoundEventAccessor createAccessor(SoundHandler handler) {
        SoundEventAccessor soundEvent = handler.getAccessor(resource);
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
        return false;
    }

    @Override
    public int getRepeatDelay() {
        return 0;
    }

    @Override
    public float getVolume() {
        return vol;
    }

    @Override
    public float getPitch() {
        return pitch;
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
        return AttenuationType.NONE;
    }

}
