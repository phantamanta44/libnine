package xyz.phanta.libnine.util.data

import com.google.common.io.ByteStreams
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTSizeTracker
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import java.nio.charset.StandardCharsets
import java.util.*

class ByteWriter {

    private val buffer: MutableList<ByteArray> = mutableListOf()
    private var length: Int = 0

    fun bytes(data: ByteArray): ByteWriter = also {
        buffer += data
        length += data.size
    }

    fun byte(data: Byte): ByteWriter = bytes(byteArrayOf(data))

    fun int(data: Int): ByteWriter {
        val bytes = ByteArray(Integer.BYTES)
        for (k in bytes.indices) bytes[k] = (data and (0xFF shl k * 8) shr k * 8).toByte()
        return bytes(bytes)
    }

    fun float(data: Float): ByteWriter = int(data.toRawBits())

    fun double(data: Double): ByteWriter = long(data.toRawBits())

    fun short(data: Short): ByteWriter {
        val asInt = data.toInt()
        val bytes = ByteArray(java.lang.Short.BYTES)
        for (k in bytes.indices) bytes[k] = (asInt and (0xFF shl k * 8) shr k * 8).toByte()
        return bytes(bytes)
    }

    fun long(data: Long): ByteWriter {
        val bytes = ByteArray(java.lang.Long.BYTES)
        for (k in bytes.indices) bytes[k] = (data and (0xFFL shl k * 8) shr k * 8).toByte()
        return bytes(bytes)
    }

    fun bool(data: Boolean): ByteWriter = byte(if (data) 1.toByte() else 0.toByte())

    fun varPrecision(data: Int): ByteWriter {
        var i = data
        while (true) {
            val afterShift = i ushr 7
            if (afterShift == 0) {
                byte((i and 0x7F or 0x80).toByte())
                break
            } else {
                byte((i and 0x7F).toByte())
            }
            i = afterShift
        }
        return this
    }

    fun string(data: String): ByteWriter {
        val bytes = data.toByteArray(StandardCharsets.UTF_8)
        return varPrecision(bytes.size).bytes(bytes)
    }

    fun enum(value: Enum<*>): ByteWriter = short(value.ordinal.toShort())

    fun resourceLocation(loc: ResourceLocation): ByteWriter = string(loc.namespace).string(loc.path)

    fun tagCompound(tag: CompoundNBT): ByteWriter {
        val buf = ByteStreams.newDataOutput()
        CompressedStreamTools.write(tag, buf)
        val bytes = buf.toByteArray()
        return varPrecision(bytes.size).bytes(bytes)
    }

    fun itemStack(stack: ItemStack): ByteWriter = tagCompound(CompoundNBT().also { stack.write(it) })

    fun fluid(fluid: Fluid): ByteWriter = TODO("forge doesn't have a fluid impl yet")

    fun fluidStack(stack: FluidStack): ByteWriter {
        fluid(stack.fluid).int(stack.amount)
        return if (stack.tag != null) bool(true).tagCompound(stack.tag) else bool(false)
    }

    fun blockPos(pos: BlockPos): ByteWriter = int(pos.x).int(pos.y).int(pos.z)

    fun vec3d(vec: Vec3d): ByteWriter = double(vec.x).double(vec.y).double(vec.z)

    fun uuid(id: UUID): ByteWriter = string(id.toString())

    fun toArray(): ByteArray {
        val dest = ByteArray(length)
        var pointer = 0
        for (chunk in buffer) {
            chunk.copyInto(dest, pointer)
            pointer += chunk.size
        }
        return dest
    }

}

class ByteReader(private val buffer: ByteArray) {

    private var pointer: Int = 0

    fun rewind(bytes: Int): ByteReader {
        if (bytes < 0) throw IllegalArgumentException("Negative rewind!")
        if (pointer > bytes) throw IndexOutOfBoundsException("Cannot rewind $bytes bytes from pointer $pointer!")
        pointer -= bytes
        return this
    }

    fun bytes(length: Int): ByteArray {
        pointer += length
        return buffer.copyOfRange(pointer - length, pointer)
    }

    fun byte(): Byte = buffer[pointer++]

    fun int(): Int {
        var value = 0
        for (i in 0 until Integer.BYTES) {
            value = value or (java.lang.Byte.toUnsignedInt(buffer[pointer + i]) shl i * 8)
        }
        pointer += Integer.BYTES
        return value
    }

    fun float(): Float {
        return java.lang.Float.intBitsToFloat(int())
    }

    fun double(): Double {
        return java.lang.Double.longBitsToDouble(long())
    }

    fun short(): Short {
        var value = 0
        for (i in 0 until java.lang.Short.BYTES) {
            value = value or (java.lang.Byte.toUnsignedInt(buffer[pointer + i]) shl i * 8)
        }
        pointer += java.lang.Short.BYTES
        return value.toShort()
    }

    fun long(): Long {
        var value: Long = 0
        for (i in 0 until java.lang.Long.BYTES) {
            value = value or (java.lang.Byte.toUnsignedLong(buffer[pointer + i]) shl i * 8)
        }
        pointer += java.lang.Long.BYTES
        return value
    }

    fun bool(): Boolean {
        return byte().toInt() != 0
    }

    fun varPrecision(): Int {
        var value = 0
        var i = 0
        var chunk: Int
        do {
            chunk = byte().toInt()
            value = value or (chunk and 127 shl 7 * i++)
        } while (chunk and 128 == 0)
        return value
    }

    fun string(): String {
        val length = varPrecision()
        pointer += length
        return String(buffer, pointer - length, length, StandardCharsets.UTF_8)
    }

    inline fun <reified E : Enum<E>> enum(): E = enumValues<E>()[short().toInt()]

    fun <E : Enum<E>> enum(enumType: Class<E>): E = enumType.enumConstants[short().toInt()]

    fun resourceLocation(): ResourceLocation = ResourceLocation(string(), string())

    fun tagCompound(): CompoundNBT {
        val length = varPrecision()
        return CompressedStreamTools.read(ByteStreams.newDataInput(bytes(length)), NBTSizeTracker.INFINITE)
    }

    fun itemStack(): ItemStack = ItemStack.read(tagCompound())

    fun fluid(): Fluid = TODO("forge doesn't have a fluid impl yet")

    fun fluidStack(): FluidStack {
        val fluid = fluid()
        val amount = int()
        val stack = FluidStack(fluid, amount)
        if (bool()) stack.tag = tagCompound()
        return stack
    }

    fun blockPos(): BlockPos = BlockPos(int(), int(), int())

    fun vec3d(): Vec3d = Vec3d(double(), double(), double())

    fun uuid(): UUID = UUID.fromString(string())

}
