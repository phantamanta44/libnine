package xyz.phanta.libnine.util.data.daedalus

import net.minecraft.nbt.CompoundNBT
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Persistent(val value: String = "")

class Daedalus<T : Any>(private val target: T) : Serializable {

    companion object {

        private val mappings: MutableMap<KClass<*>, TypeMapping<*>> = mutableMapOf()

        @Suppress("UNCHECKED_CAST")
        private fun <T : Any> calculateMappings(type: KClass<out T>): TypeMapping<T> = mappings.computeIfAbsent(type) {
            (type.allSuperclasses + type)
                    .flatMap { it.declaredMemberProperties }
                    .filterIsInstance<KProperty1<T, Any>>()
                    .mapNotNull {
                        it.findAnnotation<Persistent>()?.let { annot -> wrapProperty(getPropertyName(annot.value, it), it) }
                    }
                    .sortedBy { it.name }
        } as TypeMapping<T>

        private fun getPropertyName(annotValue: String, property: KProperty1<*, *>) =
                if (annotValue.isEmpty()) property.name else annotValue

        @Suppress("UNCHECKED_CAST")
        private fun <T : Any> wrapProperty(name: String, property: KProperty1<T, Any>): PersistentProperty<T> {
            property.isAccessible = true
            return if (property.getter.returnType.isSubtypeOf(Serializable::class.starProjectedType)) {
                SerializableProperty(name, property as KProperty1<T, Serializable>)
            } else {
                SerializerBackedProperty(name, property as KMutableProperty1<T, Any>)
            }
        }

    }

    private val mapping: TypeMapping<T> = calculateMappings(target::class)

    override fun serNbt(tag: CompoundNBT) = mapping.forEach { it.extractNbt(tag, target) }

    override fun deserNbt(tag: CompoundNBT) = mapping.forEach { it.injectNbt(tag, target) }

    override fun serByteStream(stream: ByteWriter) = mapping.forEach { it.extractByteStream(stream, target) }

    override fun deserByteStream(stream: ByteReader) = mapping.forEach { it.injectByteStream(stream, target) }

}

private typealias TypeMapping<T> = List<PersistentProperty<T>>

private interface PersistentProperty<T : Any> {

    val name: String

    fun extractNbt(tag: CompoundNBT, target: T)

    fun injectNbt(tag: CompoundNBT, target: T)

    fun extractByteStream(stream: ByteWriter, target: T)

    fun injectByteStream(stream: ByteReader, target: T)

}

private class SerializableProperty<T : Any>(override val name: String, private val property: KProperty1<T, Serializable>)
    : PersistentProperty<T> {

    override fun extractNbt(tag: CompoundNBT, target: T) {
        tag.put(name, property.get(target).createSerializedNbt())
    }

    override fun injectNbt(tag: CompoundNBT, target: T) = property.get(target).deserNbt(tag.getCompound(name))

    override fun extractByteStream(stream: ByteWriter, target: T) = property.get(target).serByteStream(stream)

    override fun injectByteStream(stream: ByteReader, target: T) = property.get(target).deserByteStream(stream)

}

private class SerializerBackedProperty<T : Any, U : Any>(override val name: String, private val property: KMutableProperty1<T, U>)
    : PersistentProperty<T> {

    @Suppress("UNCHECKED_CAST")
    private val serializer: DataSerializer<U> = DataSerializers.getSerializer(property.returnType.jvmErasure as KClass<U>)

    override fun extractNbt(tag: CompoundNBT, target: T) = serializer.serializeNbt(tag, name, property.get(target))

    override fun injectNbt(tag: CompoundNBT, target: T) = property.set(target, serializer.deserializeNbt(tag, name))

    override fun extractByteStream(stream: ByteWriter, target: T) = serializer.serializeByteStream(stream, property.get(target))

    override fun injectByteStream(stream: ByteReader, target: T) = property.set(target, serializer.deserializeByteStream(stream))

}
