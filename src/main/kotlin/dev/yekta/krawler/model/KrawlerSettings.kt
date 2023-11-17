package dev.yekta.krawler.model

import dev.yekta.krawler.log.Log
import kotlinx.serialization.Serializable

@Serializable
data class KrawlerSettings(
    val seeds: List<String>,
    val filter: CrawlingFilter,
    val depth: Int = 4,
    val maxPages: Int? = null,
    val maxPageSizeKb: Int? = null,
    val concurrentConnections: Int = 4,
    val verbose: Boolean = false,
    val shouldFollowRedirects: Boolean = true,
    val userAgent: String = "Krawler",
    val connectTimeoutMs: Long = 10_000,
    val readTimeoutMs: Long = 10_000,
    val retriesOnServerError: Int = 0,
    val customHeaders: List<Pair<String, String>>? = listOf("saf" to "dsfsdf"),
) {
    val maxPageSizeBytes = maxPageSizeKb?.times(1000)

    init {
        require(seeds.isNotEmpty()) { "No Seeds are provided!" }
        require(depth <= 100) { "Depth must be less than or equal to 100." }
        require(depth > 0) { "Depth must be positive." }
        require(maxPages == null || maxPages > 0) { "`maxPages` must be positive if it's provided." }
        require(maxPageSizeKb == null || maxPageSizeKb > 0) { "`maxPageSizeKb` must be positive if it's provided." }
        require(concurrentConnections > 0) { "Concurrent connections must be positive!" }
        require(concurrentConnections <= 10_000) { "Concurrent connections cannot be more than 10K!" }
        require(connectTimeoutMs >= 10) { "`connectTimeoutMs` cannot be less than 10!" }
        require(readTimeoutMs >= 10) { "`readTimeoutMs` cannot be less than 10!" }
        require(retriesOnServerError >= 0) { "`retriesOnServerError` cannot be negative!" }
        require(customHeaders?.isEmpty() != true) { "`customHeaders` cannot be empty once it's provided." }

        Log.verbose = verbose
    }
}
