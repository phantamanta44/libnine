package xyz.phanta.libnine.util.math

import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i

operator fun Vec3i.component1(): Int = this.x
operator fun Vec3i.component2(): Int = this.y
operator fun Vec3i.component3(): Int = this.z

operator fun Vec2f.component1(): Float = this.x
operator fun Vec2f.component2(): Float = this.y

operator fun Vec3d.component1(): Double = this.x
operator fun Vec3d.component2(): Double = this.y
operator fun Vec3d.component3(): Double = this.z
