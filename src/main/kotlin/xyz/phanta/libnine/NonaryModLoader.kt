package xyz.phanta.libnine

import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.EventBusErrorMessage
import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.LifecycleEventProvider
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider
import net.minecraftforge.forgespi.language.ILifecycleEvent
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

class NonaryProvider : IModLanguageProvider {

    override fun name(): String = "nonary"

    override fun getFileVisitor(): Consumer<ModFileScanData> = Consumer { scan ->
        scan.addLanguageLoader(
                scan.annotations
                        .filter { it.annotationType == FMLJavaModLanguageProvider.MODANNOTATION }
                        .map { NonaryLoader(it.classType.className, it.annotationData["value"] as String) }
                        .associateBy { it.modId }
        )
    }

    override fun <R : ILifecycleEvent<R>?> consumeLifecycleEvent(consumeEvent: Supplier<R>?) {
        // NO-OP
    }

}

private const val CONTAINER_TYPE = "xyz.phanta.libnine.NonaryModContainer"

private class NonaryLoader(private val modClass: String, internal val modId: String) : IModLanguageProvider.IModLanguageLoader {

    @Suppress("UNCHECKED_CAST")
    override fun <T> loadMod(info: IModInfo, modLoader: ClassLoader, scan: ModFileScanData): T {
        return Class.forName(CONTAINER_TYPE, true, Thread.currentThread().contextClassLoader)
                .getConstructor(IModInfo::class.java, Any::class.java, ModFileScanData::class.java)
                .newInstance(info, Class.forName(modClass, true, modLoader).kotlin.objectInstance, scan) as T
    }

}

private typealias LifecycleHandler = Consumer<LifecycleEventProvider.LifecycleEvent>

internal class NonaryModContainer(info: IModInfo, private val modInstance: Any, private val scan: ModFileScanData) : ModContainer(info) {

    private val logger: Logger = LogManager.getLogger("Nonary-${info.modId}")
    private val eventBus: IEventBus = BusBuilder.builder()
            .setExceptionHandler { _, event, handlers, index, error ->
                logger.error(EventBusErrorMessage(event, index, handlers, error))
            }
            .setTrackPhases(false)
            .build()
    private val registryHandler: RegistryHandler = RegistryHandler(this)

    init {
        modInstance as Virtue
        modInstance.modId = info.modId
        configHandler = Optional.of(Consumer { event -> this.eventBus.post(event) })
        contextExtension = Supplier { null }

        triggerMap[ModLoadingStage.CONSTRUCT] = LifecycleHandler { modInstance.initialize(eventBus, registryHandler) }
        LifecycleHandler { eventBus.post(it.getOrBuildEvent(this)) }.let { fireEvent ->
            listOf(
                    ModLoadingStage.CREATE_REGISTRIES, ModLoadingStage.COMMON_SETUP, ModLoadingStage.SIDED_SETUP,
                    ModLoadingStage.ENQUEUE_IMC, ModLoadingStage.PROCESS_IMC, ModLoadingStage.COMPLETE
            ).forEach { triggerMap[it] = fireEvent }
        }
        triggerMap[ModLoadingStage.LOAD_REGISTRIES] = registryHandler
    }

    override fun getMod(): Any = modInstance

    override fun matches(mod: Any): Boolean = modInstance === mod

}

internal class RegistryHandler(private val modContainer: ModContainer) : Consumer<LifecycleEventProvider.LifecycleEvent> {

    private val handlers: MutableMap<Class<out IForgeRegistryEntry<*>>, MutableList<(IForgeRegistry<*>) -> Unit>> = mutableMapOf()

    override fun accept(event: LifecycleEventProvider.LifecycleEvent) {
        val reg = (event.getOrBuildEvent(modContainer) as RegistryEvent.Register<*>).getRegistry()
        handlers[reg.getRegistrySuperType()]?.forEach { it(reg) }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : IForgeRegistryEntry<T>> registerProvider(noinline provider: (IForgeRegistry<T>) -> Unit) {
        handlers.computeIfAbsent(T::class.java) { mutableListOf() } += provider as (IForgeRegistry<*>) -> Unit
    }

}
