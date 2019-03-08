package xyz.phanta.libnine.recipe.parser

import com.electronwill.nightconfig.core.ConfigFormat
import com.electronwill.nightconfig.core.UnmodifiableConfig
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.resources.IResource
import net.minecraft.tags.ItemTags
import net.minecraft.tags.Tag
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries
import xyz.phanta.libnine.recipe.Recipe
import xyz.phanta.libnine.recipe.RecipeParser

class NightConfigRecipeParser<I, O, R : Recipe<I, O>>(
        private val format: ConfigFormat<*>,
        private val factory: NightConfigMarshal.(UnmodifiableConfig) -> Collection<R>
) : RecipeParser<I, O, R> {

    override fun isValid(fileName: String): Boolean = fileName.endsWith(".toml")

    override fun parse(resource: IResource): Collection<R> = resource.inputStream.use {
        factory(NightConfigMarshal, format.createParser().parse(it))
    }

}

object NightConfigMarshal {

    fun UnmodifiableConfig.getResourceLocation(path: String): ResourceLocation = ResourceLocation(this.get<String>(path))

    fun UnmodifiableConfig.getItem(path: String): () -> Item = {
        ForgeRegistries.ITEMS.getValue(this.getResourceLocation(path))
                ?: throw NoSuchElementException(this.get<String>(path))
    }

    fun UnmodifiableConfig.getItemTag(path: String): Tag<Item> =
            ItemTags.getCollection().get(this.getResourceLocation(path))
                    ?: throw NoSuchElementException(this.get<String>(path))

    fun UnmodifiableConfig.asItemStack(): ItemStack {
        return ItemStack(this.getItem("item"), this.getIntOrElse("count", 1))
    }

}
