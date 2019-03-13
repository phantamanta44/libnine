package xyz.phanta.libnine

import net.minecraft.client.renderer.model.ModelResourceLocation
import net.minecraft.util.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.loading.FMLEnvironment
import xyz.phanta.libnine.container.ContainerHandler
import xyz.phanta.libnine.definition.CommonInitContext
import xyz.phanta.libnine.definition.InitializationContext
import xyz.phanta.libnine.definition.ServerInitContext
import xyz.phanta.libnine.network.NetworkHandler
import xyz.phanta.libnine.network.PacketClientContainerInteraction
import xyz.phanta.libnine.network.PacketServerSyncRecipeSet
import xyz.phanta.libnine.network.PacketServerSyncTileEntity
import xyz.phanta.libnine.util.render.TextureResource
import java.util.concurrent.ConcurrentHashMap

abstract class Virtue {

    companion object {

        private val virtueMap: MutableMap<String, Virtue> = ConcurrentHashMap()
        internal val containerMap: MutableMap<Class<*>, Virtue> = ConcurrentHashMap()

        fun forModId(modId: String): Virtue? = virtueMap[modId]

        fun forContainer(containerType: Class<*>): Virtue = containerMap[containerType]!!

    }

    // mod identity

    var modId: String = "nonary_invalid"
        internal set(value) {
            field = value
            modPrefix = "$value:"
        }
    private lateinit var modPrefix: String

    // handlers

    val netHandler: NetworkHandler by lazy { NetworkHandler(resource("main")) }
    val containerHandler: ContainerHandler by lazy { ContainerHandler(this) }
    val wsdHandler: Unit by lazy { TODO("wsd impl") }

    // internal state

    private var usesTileEntities: Boolean = false
    private var usesContainers: Boolean = false
    private var usesRecipes: Boolean = false

    // init

    internal fun initialize(eventBus: IEventBus, regHandler: RegistryHandler) {
        virtueMap[modId] = this
        init(if (FMLEnvironment.dist == Dist.CLIENT) {
            CommonInitContext(this, eventBus, regHandler)
        } else {
            ServerInitContext(this, eventBus, regHandler)
        })
    }

    protected abstract fun init(context: InitializationContext)

    // internal state mutators

    internal fun markUsesTileEntities() {
        if (!usesTileEntities) {
            usesTileEntities = true
            netHandler.registerType(255, PacketServerSyncTileEntity)
        }
    }

    internal fun markUsesContainers() {
        if (!usesContainers) {
            usesContainers = true
            netHandler.registerType(254, PacketClientContainerInteraction)
        }
    }

    internal fun markUsesRecipes() {
        if (!usesRecipes) {
            usesRecipes = true
            netHandler.registerType(253, PacketServerSyncRecipeSet)
        }
    }

    // api

    fun prefix(str: String): String = "$modPrefix$str"

    fun resource(resource: String): ResourceLocation = ResourceLocation(modId, resource)

    fun modelResource(resource: String): ModelResourceLocation = ModelResourceLocation(prefix(resource))

    fun modelResource(resource: String, variant: String): ModelResourceLocation =
            ModelResourceLocation(prefix(resource), variant)

    fun textureResource(resource: String, width: Int, height: Int): TextureResource =
            TextureResource(resource(resource), width, height)

}
