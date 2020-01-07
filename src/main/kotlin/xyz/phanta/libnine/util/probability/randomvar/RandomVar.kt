package xyz.phanta.libnine.util.probability.randomvar

import kotlin.random.Random
import kotlin.random.asKotlinRandom

interface RandomVar<T> {

    fun sample(rand: Random = Random.Default): T

    fun sample(rand: java.util.Random): T = sample(rand.asKotlinRandom())

}

class ConstantVar<T>(val value: T) : RandomVar<T> {

    override fun sample(rand: Random): T = value

}

fun <I1, I2, O> RandomVar<I1>.join(i2: RandomVar<I2>, joinFun: (I1, I2) -> O): RandomVar<O> = object : RandomVar<O> {
    override fun sample(rand: Random): O = joinFun(this@join.sample(rand), i2.sample(rand))
}

fun <I1, I2, I3, O> RandomVar<I1>.join(
        i2: RandomVar<I2>,
        i3: RandomVar<I3>,
        joinFun: (I1, I2, I3) -> O
): RandomVar<O> = object : RandomVar<O> {
    override fun sample(rand: Random): O = joinFun(this@join.sample(rand), i2.sample(rand), i3.sample(rand))
}

fun <I1, I2, I3, I4, O> RandomVar<I1>.join(
        i2: RandomVar<I2>,
        i3: RandomVar<I3>,
        i4: RandomVar<I4>,
        joinFun: (I1, I2, I3, I4) -> O
): RandomVar<O> = object : RandomVar<O> {
    override fun sample(rand: Random): O = joinFun(this@join.sample(rand), i2.sample(rand), i3.sample(rand), i4.sample(rand))
}
