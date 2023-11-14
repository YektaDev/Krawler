package dev.yekta.krawler.repo

import dev.yekta.krawler.model.CrawlingSessionID

interface CrawlActivityStore {
    fun addStart(session: CrawlingSessionID, epochSeconds: Long)
    fun addStop(session: CrawlingSessionID, epochSeconds: Long)

    fun addPause(session: CrawlingSessionID, epochSeconds: Long)
    fun addResume(session: CrawlingSessionID, epochSeconds: Long)
}