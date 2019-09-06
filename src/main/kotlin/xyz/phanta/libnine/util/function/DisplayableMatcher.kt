package xyz.phanta.libnine.util.function

interface DisplayableMatcher<T> {

    val visuals: List<T>
    val visual: T?
        get() = visuals.firstOrNull()

    fun test(value: T): Boolean

    companion object {

        fun <T> of(visuals: List<T>, matcher: (T) -> Boolean): DisplayableMatcher<T> = object : DisplayableMatcher<T> {
            override val visuals: List<T> = visuals
            override fun test(value: T): Boolean = matcher(value)
        }

        fun <T> of(vararg visuals: T, matcher: (T) -> Boolean): DisplayableMatcher<T> = of(visuals.toList(), matcher)

    }

}
