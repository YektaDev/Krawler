package dev.yekta.krawler.repo

import dev.yekta.krawler.model.CrawlingSessionID

interface CrawlErrorStore {
    fun add(sessionID: CrawlingSessionID, url: String, error: String)
}
