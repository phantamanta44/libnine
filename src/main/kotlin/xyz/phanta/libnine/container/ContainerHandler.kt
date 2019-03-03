package xyz.phanta.libnine.container

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.IInteractionObject
import net.minecraftforge.fml.ExtensionPoint
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.network.FMLPlayMessages
import net.minecraftforge.fml.network.NetworkHooks
import xyz.phanta.libnine.Virtue
import xyz.phanta.libnine.client.gui.NineGuiContainer
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter

class ContainerHandler(private val mod: Virtue) : java.util.function.Function<FMLPlayMessages.OpenContainer, GuiScreen> {

    private val containerTypeMappings: MutableMap<ResourceLocation, ContainerType<*, *, *>> = mutableMapOf()

    init {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY) { this }
    }

    fun register(type: ContainerType<*, *, *>) {
        containerTypeMappings[type.key] = type
        Virtue.containerMap[type.containerClass] = mod
    }

    @Suppress("UNCHECKED_CAST")
    override fun apply(packet: FMLPlayMessages.OpenContainer): GuiScreen {
        val type = containerTypeMappings[packet.id]!! as ContainerType<NineContainer, NineGuiContainer, Any>
        val player = Minecraft.getInstance().player
        return type.guiFactory(
                type.containerFactory(
                        type.contextDeserializer(ByteReader(packet.additionalData.readByteArray())),
                        player.inventory,
                        player
                )
        )
    }

}

fun <X> EntityPlayer.openContainer(type: ContainerType<*, *, X>, context: X) {
    if (this is EntityPlayerMP) {
        NetworkHooks.openGui(this, type.generateContainerProvider(context)) { buf ->
            buf.writeByteArray(ByteWriter().also { type.contextSerializer(it, context) }.toArray())
        }
    }
}

class ContainerType<C : NineContainer, G : NineGuiContainer, X>(
        internal val key: ResourceLocation,
        internal val containerClass: Class<C>,
        internal val containerFactory: (X, InventoryPlayer, EntityPlayer) -> C,
        internal val contextSerializer: (ByteWriter, X) -> Unit,
        internal val contextDeserializer: (ByteReader) -> X,
        internal val guiFactory: (C) -> G
) {

    override fun hashCode(): Int = key.hashCode()

    override fun equals(other: Any?): Boolean = other is ContainerType<*, *, *> && other.key == key

    internal fun generateContainerProvider(context: X): IInteractionObject = DummyInteractionObject(context)

    private inner class DummyInteractionObject(private val context: X) : IInteractionObject {

        override fun hasCustomName(): Boolean = throw UnsupportedOperationException()

        override fun getCustomName(): ITextComponent? = throw UnsupportedOperationException()

        override fun getName(): ITextComponent = throw UnsupportedOperationException()

        override fun getGuiID(): String = name.toString()

        override fun createContainer(playerInventory: InventoryPlayer, playerIn: EntityPlayer): Container =
                containerFactory(context, playerInventory, playerIn)

    }

}
