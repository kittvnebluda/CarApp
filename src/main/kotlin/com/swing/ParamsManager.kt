package com.swing

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class ParamsManager<T>(
    private val configPath: Path,
    private val defaultParams: T,
    private val serializer: KSerializer<T>
) {

    var params: T = defaultParams

    fun loadParams() {
        try {
            params = Json.decodeFromString(serializer, Files.readString(configPath))
            println("Loaded params: $params")
        } catch (e: NoSuchFileException) {
            println("Config file not found: $configPath")
            println("Creating new one with default parameters")
            params = defaultParams
            saveParams()
        } catch (e: SerializationException) {
            println("Something went wrong while deserializing config file: $configPath")
            println("Using default parameters")
            params = defaultParams
        }
    }

    fun saveParams() {
        Files.writeString(configPath, Json.encodeToString(serializer, params), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        println("Saved params: $params")
    }
}