package xyz.phanta.libnine.util

import net.minecraft.tags.Tag
import xyz.phanta.libnine.util.function.DisplayableMatcher

fun <T> Tag<T>.matcher(): DisplayableMatcher<T> = DisplayableMatcher.of<T>(this.allElements.toList()) { this.contains(it) }
