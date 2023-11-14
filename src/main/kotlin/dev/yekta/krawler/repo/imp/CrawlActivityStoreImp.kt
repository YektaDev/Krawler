package dev.yekta.krawler.repo.imp

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.repo.CrawlActivityStore

class CrawlActivityStoreImp:CrawlActivityStore {
    override fun addStart(session: CrawlingSessionID, epochSeconds: Long) {
        TODO("Not yet implemented")
    }

    override fun addStop(session: CrawlingSessionID, epochSeconds: Long) {
        TODO("Not yet implemented")
    }

    override fun addPause(session: CrawlingSessionID, epochSeconds: Long) {
        TODO("Not yet implemented")
    }

    override fun addResume(session: CrawlingSessionID, epochSeconds: Long) {
        TODO("Not yet implemented")
    }
}
