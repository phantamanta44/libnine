package xyz.phanta.libnine.util.world

import net.minecraft.util.Direction

fun Direction.isVertical(): Boolean = this == Direction.UP || this == Direction.DOWN
fun Direction.isHorizontal(): Boolean = !this.isVertical()
