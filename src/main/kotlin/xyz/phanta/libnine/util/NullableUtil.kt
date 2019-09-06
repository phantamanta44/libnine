package xyz.phanta.libnine.util

import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun <T> LazyOptional<T>.orNull(): T? = this.orElse(null)

fun <T> ICapabilityProvider.maybeCap(cap: Capability<T>, side: Direction? = null): T? = getCapability(cap, side).orNull()

fun ItemStack.maybeTag(): CompoundNBT? = if (hasTag()) tag!! else null
