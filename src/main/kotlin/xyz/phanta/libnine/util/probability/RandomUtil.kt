package xyz.phanta.libnine.util.probability

import kotlin.random.Random

fun Random.bernoulli(p: Double = 0.5) = nextDouble() < p
fun java.util.Random.bernoulli(p: Double = 0.5) = nextDouble() < p

fun Random.binomial(n: Int, p: Double = 0.5) = (1..n).sumBy { if (bernoulli(p)) 1 else 0 }
fun java.util.Random.bernoulli(n: Int, p: Double = 0.5) = (1..n).sumBy { if (bernoulli(p)) 1 else 0 }
