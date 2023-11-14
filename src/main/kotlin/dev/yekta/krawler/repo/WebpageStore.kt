package dev.yekta.krawler.repo

import dev.yekta.krawler.model.CrawlingSessionID

interface WebpageStore {
    fun add(session: CrawlingSessionID, url: String, html: String)
}