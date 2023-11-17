package dev.yekta.krawler.repo

import dev.yekta.krawler.model.CrawlingSessionID

interface WebpageStore {
    suspend fun add(session: CrawlingSessionID, epochSeconds: Long, url: String, html: String)
}