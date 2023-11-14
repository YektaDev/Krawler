package dev.yekta.krawler.repo.imp

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.model.StoredUrl
import dev.yekta.krawler.repo.CrawlingStateStore

class CrawlingStateStoreImp : CrawlingStateStore {
    override fun maxPriority(session: CrawlingSessionID): StoredUrl? {
        TODO("Not yet implemented")
    }

    override fun minPriority(session: CrawlingSessionID): StoredUrl? {
        TODO("Not yet implemented")
    }

    override fun add(url: StoredUrl) {
        TODO("Not yet implemented")
    }

    override fun remove(session: CrawlingSessionID, url: String) {
        TODO("Not yet implemented")
    }
}
