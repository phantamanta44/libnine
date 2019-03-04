package xyz.phanta.libnine.recipe

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.resources.IResourceManager
import net.minecraft.resources.IResourceManagerReloadListener
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.gameevent.PlayerEvent
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent
import net.minecraftforge.fml.network.PacketDistributor
import xyz.phanta.libnine.definition.Registrar
import xyz.phanta.libnine.network.PacketServerSyncRecipeSet
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.getResource
import java.util.concurrent.ConcurrentHashMap

class RecipeSet<I, O>(private val type: RecipeType<I, O, out Recipe<I, O>>) : IResourceManagerReloadListener {

    companion object {

        private val recipeSetMapping: MutableMap<ResourceLocation, RecipeSet<*, *>> = ConcurrentHashMap()
        private val recipeTypeMapping: MutableMap<ResourceLocation, RecipeType<*, *, *>> = ConcurrentHashMap()

        internal fun <I, O> registerType(type: RecipeType<I, O, out Recipe<I, O>>, reg: Registrar) {
            RecipeSet(type).let {
                recipeSetMapping[type.name] = it
                reg.bus.addListener<PlayerEvent.PlayerLoggedInEvent> { event ->
                    reg.mod.netHandler.postToClient(
                            PacketDistributor.PLAYER.with { event.player as EntityPlayerMP },
                            PacketServerSyncRecipeSet.Packet(type, it.recipes)
                    )
                }
                reg.bus.addListener<FMLServerAboutToStartEvent> { event ->
                    event.server.resourceManager.addReloadListener(it)
                }
            }
            recipeTypeMapping[type.name] = type
        }

        @Suppress("UNCHECKED_CAST")
        internal fun <I, O> setForType(type: RecipeType<I, O, *>): RecipeSet<I, O> =
                recipeSetMapping[type.name] as RecipeSet<I, O>

        @Suppress("UNCHECKED_CAST")
        internal fun <I, O, R : Recipe<I, O>> typeForName(name: ResourceLocation): RecipeType<I, O, R> =
                recipeTypeMapping[name] as RecipeType<I, O, R>

    }

    internal val recipes: MutableList<Recipe<I, O>> = mutableListOf()

    override fun onResourceManagerReload(resourceManager: IResourceManager) {
        recipes.clear()
        resourceManager.getAllResourceLocations("${type.name.namespace}/${type.name.path}", type.parser::isValid)
                .forEach { recipes += type.parser.parse(it.getResource()) }
    }

}

class RecipeType<I, O, R : Recipe<I, O>>(
        internal val name: ResourceLocation,
        internal val parser: RecipeParser<I, O, R>,
        internal val serializer: (ByteWriter, R) -> Unit,
        internal val deserializer: (ByteReader) -> R
) {

    private val recipeSet: RecipeSet<I, O> by lazy { RecipeSet.setForType(this) }

    fun findRecipe(input: I): Recipe<I, O>? = recipeSet.recipes.firstOrNull { it.input.matches(input) }

    val recipes: Collection<Recipe<I, O>>
        get() = recipeSet.recipes

}
