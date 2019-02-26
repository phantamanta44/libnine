package xyz.phanta.libnine.util

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import net.minecraft.client.Minecraft
import net.minecraft.resources.IResource
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.common.thread.EffectiveSide
import net.minecraftforge.fml.server.ServerLifecycleHooks
import org.apache.commons.io.IOUtils
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

private val JSON_PARSER: JsonParser = JsonParser()

fun ResourceLocation.getResource(): IResource = if (EffectiveSide.get() == LogicalSide.CLIENT) {
    Minecraft.getInstance().resourceManager
} else {
    ServerLifecycleHooks.getCurrentServer().resourceManager
}.getResource(this)

fun IResource.asString(encoding: Charset = StandardCharsets.UTF_8): String = inputStream.use { IOUtils.toString(it, encoding) }

fun IResource.asJson(): JsonElement = InputStreamReader(inputStream).use { JSON_PARSER.parse(it) }
