package xyz.phanta.libnine.util.function

class CallbackSet0(private val callbacks: MutableSet<() -> Unit> = mutableSetOf()) {

    fun addCallback(callback: () -> Unit) {
        callbacks += callback
    }

    operator fun invoke() = callbacks.forEach { it() }

}

class CallbackSet1<A>(private val callbacks: MutableSet<(A) -> Unit> = mutableSetOf()) {

    fun addCallback(callback: (A) -> Unit) {
        callbacks += callback
    }

    operator fun invoke(a: A) = callbacks.forEach { it(a) }

}
