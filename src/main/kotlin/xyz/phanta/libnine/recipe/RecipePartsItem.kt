package xyz.phanta.libnine.recipe

import com.google.gson.JsonElement
import io.netty.buffer.Unpooled
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.Ingredient
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.util.JsonUtils
import net.minecraftforge.registries.ForgeRegistries
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.getString
import xyz.phanta.libnine.util.item.ProbabilisticStack
import xyz.phanta.libnine.util.probability.randomvar.ConstantVar

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
        override fun deserialize(dto: JsonElement): ItemStack = CraftingHelper.getItemStack(dto.asJsonObject, true)

        override fun read(stream: ByteReader): ItemStack = stream.itemStack()

        override fun write(stream: ByteWriter, obj: ItemStack) {
            stream.itemStack(obj)
        }
    }

    object ProbStack : RecipePart<ProbabilisticStack> {
        override fun deserialize(dto: JsonElement): ProbabilisticStack {
            val stackDto = dto.asJsonObject
            val itemId = ResourceLocation(stackDto.getString("item"))
            return ProbabilisticStack(
                    ForgeRegistries.ITEMS.getValue(itemId) ?: throw NoSuchElementException("Unknown item: $itemId"),
                    stackDto.get("count")?.let { RecipePartsProbabilistic.RandomInt.deserialize(it) } ?: ConstantVar(1),
                    JsonUtils.readNBT(stackDto, "nbt")
            )
        }

        override fun read(stream: ByteReader): ProbabilisticStack {
            val itemId = stream.resourceLocation()
            return ProbabilisticStack(
                    ForgeRegistries.ITEMS.getValue(itemId) ?: throw NoSuchElementException("Unknown item: $itemId"),
                    RecipePartsProbabilistic.RandomInt.read(stream),
                    if (stream.bool()) stream.tagCompound() else null
            )
        }

        override fun write(stream: ByteWriter, obj: ProbabilisticStack) {
            stream.resourceLocation(obj.item.registryName!!)
            RecipePartsProbabilistic.RandomInt.write(stream, obj.countVar)
            obj.nbt?.let { stream.bool(true).tagCompound(it) } ?: stream.bool(false)
        }
    }

}
