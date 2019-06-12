package xyz.phanta.libnine.util.data.daedalus

import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import kotlin.math.ceil

class ByteStreamDeltaMarker(private val bitCount: () -> Int) {

    private val byteCount: Int by lazy { ceil(bitCount() / 8.0).toInt() }
    private val fullBitField: ByteArray = ByteArray(byteCount) { 0xFF.toByte() }

    fun createField(): MarkerField = MarkerField(ByteArray(byteCount))

    fun readField(stream: ByteReader): MarkerField = MarkerField(stream.bytes(byteCount))

    fun writeFullField(stream: ByteWriter) {
        stream.bytes(fullBitField)
    }

    inner class MarkerField(private val bytes: ByteArray) {

        operator fun get(index: Int): Boolean {
            val residual = index % 8
            return bytes[(index - residual) / 8].toInt() and (1 shl residual) != 0
        }

        fun set(index: Int) {
            val residual = index % 8
            val byteIndex = (index - residual) / 8
            bytes[byteIndex] = (bytes[byteIndex].toInt() or (1 shl residual)).toByte()
        }

        fun write(stream: ByteWriter) {
            stream.bytes(bytes)
        }

    }

}
