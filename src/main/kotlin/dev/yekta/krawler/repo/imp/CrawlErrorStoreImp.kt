package dev.yekta.krawler.repo.imp

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.repo.CrawlErrorStore

class CrawlErrorStoreImp:CrawlErrorStore {
    override fun add(sessionID: CrawlingSessionID, url: String, error: String) {
        TODO("Not yet implemented")
    }
}
