package dev.yekta.krawler.repo.imp

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.repo.WebpageStore

class WebpageStoreImp : WebpageStore {
    override fun add(session: CrawlingSessionID, url: String, html: String) {
        TODO("Not yet implemented")
    }
}
