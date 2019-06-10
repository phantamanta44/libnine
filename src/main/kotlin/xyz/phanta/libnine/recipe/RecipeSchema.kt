package xyz.phanta.libnine.recipe

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.inventory.IInventory
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistryEntry
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import kotlin.reflect.KMutableProperty1

class RecipeSchema<I : IInventory, R : IRecipe<I>>(
        val recipeType: Class<out R>,
        private val factory: () -> R,
        private val parts: Array<out RecipeComponent<R, *>>
) : ForgeRegistryEntry<IRecipeSerializer<*>>(), IRecipeSerializer<R> {

    companion object {

        inline fun <I : IInventory, reified R : IRecipe<I>> build(
                noinline factory: () -> R,
                vararg parts: RecipeComponent<R, *>
        ): RecipeSchema<I, R> = RecipeSchema(R::class.java, factory, parts)

    }

    override fun write(buffer: PacketBuffer, recipe: R) {
        val stream = ByteWriter()
        parts.forEach { it.write(stream, recipe) }
        buffer.writeByteArray(stream.toArray())
    }

    override fun read(recipeId: ResourceLocation, json: JsonObject): R =
            factory().also { parts.forEach { part -> part.deserialize(json, it) } }

    override fun read(recipeId: ResourceLocation, buffer: PacketBuffer): R {
        val stream = ByteReader(buffer.readByteArray())
        return factory().also { parts.forEach { part -> part.read(stream, it) } }
    }

}

class RecipeComponent<R, T>(val consumer: (R, T) -> Unit, val producer: (R) -> T, val part: RecipePart<T>) {

    fun deserialize(dto: JsonObject, recipe: R) {
        consumer(recipe, part.deserialize(dto.get(part.key)))
    }

    fun read(stream: ByteReader, recipe: R) {
        consumer(recipe, part.read(stream))
    }

    fun write(stream: ByteWriter, recipe: R) {
        part.write(stream, producer(recipe))
    }

}

infix fun <R : IRecipe<*>, T> KMutableProperty1<R, T>.from(part: RecipePart<T>): RecipeComponent<R, T> =
        RecipeComponent(this::set, this::get, part)

abstract class RecipePart<T>(internal val key: String) {

    abstract fun deserialize(dto: JsonElement): T

    abstract fun read(stream: ByteReader): T

    abstract fun write(stream: ByteWriter, obj: T)

}
