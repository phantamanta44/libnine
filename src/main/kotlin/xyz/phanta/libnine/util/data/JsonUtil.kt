package xyz.phanta.libnine.util.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject

fun JsonObject.tryInt(key: String): Int? = get(key)?.asInt

fun JsonObject.getInt(key: String): Int =
        tryInt(key) ?: throw NoSuchElementException("Expected integer \"$key\"!")

fun JsonObject.tryLong(key: String): Long? = get(key)?.asLong

fun JsonObject.getLong(key: String): Long =
        tryLong(key) ?: throw NoSuchElementException("Expected long \"$key\"!")

fun JsonObject.tryFloat(key: String): Float? = get(key)?.asFloat

fun JsonObject.getFloat(key: String): Float =
        tryFloat(key) ?: throw NoSuchElementException("Expected float \"$key\"!")

fun JsonObject.tryDouble(key: String): Double? = get(key)?.asDouble

fun JsonObject.getDouble(key: String): Double =
        tryDouble(key) ?: throw NoSuchElementException("Expected double \"$key\"!")

fun JsonObject.tryBool(key: String): Boolean? = get(key)?.asBoolean

fun JsonObject.getBool(key: String): Boolean =
        tryBool(key) ?: throw NoSuchElementException("Expected boolean \"$key\"!")

fun JsonObject.tryString(key: String): String? = get(key)?.asString

fun JsonObject.getString(key: String): String =
        tryString(key) ?: throw NoSuchElementException("Expected string \"$key\"!")

fun JsonObject.getObject(key: String): JsonObject =
        tryObject(key) ?: throw NoSuchElementException("Expected JSON object \"$key\"!")

fun JsonObject.tryObject(key: String): JsonObject? = get(key)?.asJsonObject

fun JsonObject.getArray(key: String): JsonArray =
        tryArray(key) ?: throw NoSuchElementException("Expected JSON array \"$key\"!")

fun JsonObject.tryArray(key: String): JsonArray? = get(key)?.asJsonArray
