package xyz.phanta.libnine.recipe

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import xyz.phanta.libnine.util.data.*
import xyz.phanta.libnine.util.probability.randomvar.*

object RecipePartsProbabilistic {

    class RandomInt : RecipePart<RandomVar<Int>> {
        override fun deserialize(dto: JsonElement): RandomVar<Int> {
            return if (dto.isJsonPrimitive) {
                ConstantVar(dto.asInt)
            } else {
                val distDto = dto.asJsonObject
                when (distDto.getString("type")) {
                    "bernoulli" -> BernoulliVar(
                            distDto.getDouble("p"),
                            distDto.getInt("outcome_p"),
                            distDto.getInt("outcome_q")
                    )
                    "uniform" -> DiscreteUniformVar((distDto.getInt("min")..distDto.getInt("max")).toList())
                    "weighted" -> WeightedVar(distDto.getArray("outcomes").map {
                        (it as JsonObject).getInt("value") to (it.tryDouble("weight") ?: 1.0)
                    })
                    "binomial" -> {
                        val offset = distDto.tryInt("offset") ?: 0
                        BinomialVar(distDto.getDouble("p"), (0..distDto.getInt("n")).map { it + offset })
                    }
                    else -> throw IllegalArgumentException("Unknown distribution: ${distDto.get("type")}")
                }
            }
        }

        override fun read(stream: ByteReader): RandomVar<Int> {
            return when (val typeId = stream.byte()) {
                0.toByte() -> ConstantVar(stream.int())
                1.toByte() -> BernoulliVar(stream.double(), stream.int(), stream.int())
                2.toByte() -> DiscreteUniformVar((1..stream.varPrecision()).map { stream.int() })
                3.toByte() -> WeightedVar((1..stream.varPrecision()).map { stream.int() to stream.double() })
                4.toByte() -> BinomialVar(stream.double(), (stream.int()..stream.int()).toList())
                else -> throw IllegalStateException("Unknown distribution type ID: 0x${typeId.toString(16)}")
            }
        }

        override fun write(stream: ByteWriter, obj: RandomVar<Int>) {
            when (obj) {
                is ConstantVar<Int> -> stream.byte(0).int(obj.value)
                is BernoulliVar<Int> -> stream.byte(1).double(obj.p).int(obj.success).int(obj.failure)
                is DiscreteUniformVar<Int> -> {
                    stream.byte(2).varPrecision(obj.outcomes.size)
                    obj.outcomes.forEach { stream.int(it) }
                }
                is WeightedVar<Int> -> {
                    stream.byte(3).varPrecision(obj.outcomes.size)
                    obj.outcomes.forEach { stream.int(it.first).double(it.second) }
                }
                is BinomialVar<Int> -> {
                    stream.byte(4).double(obj.p).int(obj.outcomes.first()).int(obj.outcomes.last())
                }
                else -> throw IllegalStateException("Cannot serialize distribution type: ${obj.javaClass.simpleName}")
            }
        }
    }

}
