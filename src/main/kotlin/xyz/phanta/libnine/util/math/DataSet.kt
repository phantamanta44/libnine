package xyz.phanta.libnine.util.math

class IntRingBuffer(sampleSize: Int) {

    private val samples: IntArray = IntArray(sampleSize)
    private var pointer: Int = 0

    fun buffer(newSample: Int) {
        pointer = (pointer + 1) % samples.size
        samples[pointer] = newSample
    }

    fun getAverageInt(): Int = samples.sum() / samples.size

    fun getAverageDouble(): Double = samples.average()

}

class DoubleRingBuffer(sampleSize: Int) {

    private val samples: DoubleArray = DoubleArray(sampleSize)
    private var pointer: Int = 0

    fun buffer(newSample: Double) {
        pointer = (pointer + 1) % samples.size
        samples[pointer] = newSample
    }

    fun getAverage(): Double = samples.average()

}
