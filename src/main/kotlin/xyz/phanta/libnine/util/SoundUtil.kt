package xyz.phanta.libnine.util

import net.minecraft.client.Minecraft
import net.minecraft.client.audio.SimpleSound
import net.minecraft.util.SoundEvent

fun SoundEvent.play() = Minecraft.getInstance().soundHandler.play(SimpleSound.master(this, 1F))
