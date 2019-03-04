package xyz.phanta.libnine.recipe

interface RecipeInput<I> {

    fun matches(input: I): Boolean

    fun consume(input: I): I

}
