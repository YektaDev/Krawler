package dev.yekta.krawler.repo

import dev.yekta.krawler.model.CrawlingSessionID

interface CrawlActivityStore {
    suspend fun addStart(session: CrawlingSessionID, epochSeconds: Long)
    suspend fun addStop(session: CrawlingSessionID, epochSeconds: Long)

    suspend fun addPause(session: CrawlingSessionID, epochSeconds: Long)
    suspend fun addResume(session: CrawlingSessionID, epochSeconds: Long)

    suspend fun isCompleted(session: CrawlingSessionID): Boolean

    suspend fun sessionDurationSeconds(session: CrawlingSessionID): Long
}
