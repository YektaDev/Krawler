package dev.yekta.krawler.settings

import dev.yekta.krawler.model.KrawlerSettings
import java.io.File

object SettingsFile {
    private const val DEFAULT_SETTINGS_PATH = "krawler_conf.json"

    fun write(path: String = DEFAULT_SETTINGS_PATH, settings: KrawlerSettings) {
        val encoded = SettingsEncoder.encode(settings)
        File(path).writeText(encoded)
    }

    fun read(path: String = DEFAULT_SETTINGS_PATH): KrawlerSettings {
        val encoded = File(path).readText()
        return SettingsEncoder.decode(encoded)
    }
}