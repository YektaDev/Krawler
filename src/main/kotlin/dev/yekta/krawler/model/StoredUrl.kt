package dev.yekta.krawler.model

data class StoredUrl(
    val session: CrawlingSessionID,
    val url: String,
    val priority: Long,
    val depth: Int,
)
