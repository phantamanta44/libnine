package xyz.phanta.libnine.util

import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import net.minecraftforge.registries.ObjectHolderRegistry
import kotlin.reflect.KMutableProperty0

fun <T : IForgeRegistryEntry<T>, U> KMutableProperty0<in U>.fromObjectHolder(
        registry: IForgeRegistry<T>,
        id: ResourceLocation,
        func: (T) -> U
) {
    ObjectHolderRegistry.addHandler { pred ->
        if (pred.test(id)) {
            @Suppress("UNCHECKED_CAST")
            registry.getValue(id)?.let { this.set(func(it)) }
        }
    }
}

fun <T : IForgeRegistryEntry<T>, U> KMutableProperty0<in U>.fromObjectHolder(
        registry: IForgeRegistry<T>,
        obj: T,
        func: (T) -> U
) = fromObjectHolder(registry, obj.registryName!!, func)
