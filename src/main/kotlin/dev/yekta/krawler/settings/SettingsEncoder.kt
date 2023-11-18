package dev.yekta.krawler.settings

import dev.yekta.krawler.model.KrawlerSettings
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SettingsEncoder {
    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        this.encodeDefaults = true
        this.prettyPrint = true
        this.allowTrailingComma = true
        this.classDiscriminator = "#"
        this.explicitNulls = true
        this.ignoreUnknownKeys = false
        this.allowSpecialFloatingPointValues = true
        this.allowStructuredMapKeys = true
    }

    fun encode(settings: KrawlerSettings) = json.encodeToString(settings)
    fun decode(string: String) = json.decodeFromString<KrawlerSettings>(string)
}