package xyz.phanta.libnine.util.probability.randomvar

import xyz.phanta.libnine.util.probability.bernoulli
import xyz.phanta.libnine.util.probability.binomial
import kotlin.random.Random

class BernoulliVar<T>(val p: Double, val success: T, val failure: T) : RandomVar<T> {

    override fun sample(rand: Random): T = if (rand.bernoulli(p)) success else failure

}

class DiscreteUniformVar<T>(val outcomes: List<T>) : RandomVar<T> {

    override fun sample(rand: Random): T = outcomes.random(rand)

}

class WeightedVar<T>(val outcomes: List<Pair<T, Double>>) : RandomVar<T> {

    private val weights: List<Double>
    private val values: List<T>
    private val maxWeight: Double

    init {
        val weights = mutableListOf<Double>()
        val values = mutableListOf<T>()
        var cumulativeWeight = 0.0
        outcomes.forEach { (outcome, weight) ->
            weights += cumulativeWeight
            cumulativeWeight += weight
            values += outcome
        }
        this.weights = weights
        this.values = values
        this.maxWeight = cumulativeWeight
    }

    override fun sample(rand: Random): T {
        val index = weights.binarySearch(rand.nextDouble(maxWeight))
        return if (index > 0) values[index] else values[-index - 2]
    }

}

class BinomialVar<T>(val p: Double, val outcomes: List<T>) : RandomVar<T> {

    override fun sample(rand: Random): T = outcomes[rand.binomial(outcomes.lastIndex, p)]

}
