package xyz.phanta.libnine

import net.minecraft.client.renderer.model.ModelResourceLocation
import net.minecraft.util.ResourceLocation
import net.minecraftforge.eventbus.api.IEventBus
import xyz.phanta.libnine.container.ContainerHandler
import xyz.phanta.libnine.definition.InitializationContext
import xyz.phanta.libnine.network.NetworkHandler
import xyz.phanta.libnine.network.PacketServerSyncTileEntity
import xyz.phanta.libnine.util.render.TextureResource
import java.util.concurrent.ConcurrentHashMap

abstract class Virtue {

    companion object {

        private val virtueMap: MutableMap<String, Virtue> = ConcurrentHashMap()

        fun forModId(modId: String): Virtue? = virtueMap[modId]

    }

    // mod identity

    var modId: String = "nonary_invalid"
        internal set
    private val modPrefix: String = "$modId:"

    // handlers

    val netHandler: NetworkHandler by lazy { NetworkHandler(resource("main")) }
    val containerHandler: ContainerHandler by lazy { ContainerHandler() }
    val wsdHandler: Unit by lazy { TODO("wsd impl") }

    // internal state

    private var usesTileEntities: Boolean = false
    private var usesContainers: Boolean = false

    // init

    internal fun initialize(eventBus: IEventBus) {
        virtueMap[modId] = this
        init(InitializationContext(this, eventBus))
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
            // TODO do something useful maybe
        }
    }

    // api

    fun prefix(str: String): String = "${modPrefix}str"

    fun resource(resource: String): ResourceLocation = ResourceLocation(modId, resource)

    fun modelResource(resource: String): ModelResourceLocation = ModelResourceLocation(prefix(resource))

    fun modelResource(resource: String, variant: String): ModelResourceLocation =
            ModelResourceLocation(prefix(resource), variant)

    fun textureResource(resource: String, width: Int, height: Int): TextureResource =
            TextureResource(resource(resource), width, height)

}
