package xyz.phanta.libnine.recipe

import com.google.gson.JsonElement
import io.netty.buffer.Unpooled
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.Ingredient
import net.minecraft.item.crafting.ShapedRecipe
import net.minecraft.network.PacketBuffer
import net.minecraftforge.common.crafting.CraftingHelper
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.probability.randomvar.RandomVar

object RecipePartsItem {

    object ItemIngredient : RecipePart<Ingredient> {
        override fun deserialize(dto: JsonElement): Ingredient = CraftingHelper.getIngredient(dto)

        override fun read(stream: ByteReader): Ingredient = CraftingHelper.getIngredient(
                stream.resourceLocation(),
                PacketBuffer(Unpooled.wrappedBuffer(stream.bytes(stream.varPrecision())))
        )

        override fun write(stream: ByteWriter, obj: Ingredient) {
            val bytes = PacketBuffer(Unpooled.buffer()).also { obj.write(it) }.array()
            stream.resourceLocation(CraftingHelper.getID(obj.serializer)!!).varPrecision(bytes.size).bytes(bytes)
        }
    }

    object Stack : RecipePart<ItemStack> {
        override fun deserialize(dto: JsonElement): ItemStack = ShapedRecipe.deserializeItem(dto.asJsonObject)

        override fun read(stream: ByteReader): ItemStack = stream.itemStack()

        override fun write(stream: ByteWriter, obj: ItemStack) {
            stream.itemStack(obj)
        }
    }

    object OutputStack : RecipePart<RandomVar<ItemStack>> {
        override fun deserialize(dto: JsonElement): RandomVar<ItemStack> {
            TODO("no impl")
        }

        override fun read(stream: ByteReader): RandomVar<ItemStack> {
            TODO("no impl")
        }

        override fun write(stream: ByteWriter, obj: RandomVar<ItemStack>) {
            TODO("no impl")
        }
    }

}
