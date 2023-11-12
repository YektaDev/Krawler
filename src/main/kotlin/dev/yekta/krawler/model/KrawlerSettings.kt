package dev.yekta.krawler.model

import kotlinx.serialization.Serializable

@Serializable
data class KrawlerSettings(
    val url: String = "",
    val depth: Int = 4,
    val maxPages: Int? = null,
    val maxDuration: Int? = null,
    val maxPageSize: Int? = null,
    val userAgent: String = "Krawler",
    val concurrentConnections: Int = 4,
    val outputDir: String = "output",
    val outputFormat: String = "json",
    val verbose: Boolean = false,
)
