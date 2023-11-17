package dev.yekta.krawler.repo

import dev.yekta.krawler.model.CrawlingSessionID

interface CrawlErrorStore {
    suspend fun add(session: CrawlingSessionID, epochSeconds: Long, url: String, error: String)
}
