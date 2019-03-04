package xyz.phanta.libnine.recipe

interface RecipeOutput<O> {

    fun isAcceptable(env: O): Boolean

    fun apply(env: O): O

}
