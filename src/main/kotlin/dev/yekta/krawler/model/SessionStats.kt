package dev.yekta.krawler.model

data class SessionStats(
    val id: CrawlingSessionID,
    val totalSeconds: Long,
    val totalCrawledPages: Long,
    val averageHtmlLength: Double,
    val urlsInQueue: Long,
)
