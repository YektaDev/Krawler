package dev.yekta.krawler.repo

import dev.yekta.krawler.model.CrawlingSessionID

interface WebpageStore {
    suspend fun add(session: CrawlingSessionID, epochSeconds: Long, url: String, html: String)

    suspend fun total(session: CrawlingSessionID): Long
    suspend fun averageHtmlLength(session: CrawlingSessionID): Double
}