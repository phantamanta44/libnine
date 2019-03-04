package xyz.phanta.libnine.recipe

import net.minecraft.resources.IResource

interface Recipe<I, O> {

    val input: RecipeInput<I>

    fun mapToOutput(input: I): RecipeOutput<O>

}

interface RecipeParser<I, O, R : Recipe<I, O>> {

    fun isValid(fileName: String): Boolean

    fun parse(resource: IResource): Collection<R>

}
