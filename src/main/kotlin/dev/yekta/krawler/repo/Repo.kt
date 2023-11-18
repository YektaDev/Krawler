package dev.yekta.krawler.repo

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.model.SessionStats

interface Repo {
    val state: CrawlingStateStore
    val webpage: WebpageStore
    val activity: CrawlActivityStore
    val error: CrawlErrorStore

    suspend fun getSessions(): List<CrawlingSessionID>
    suspend fun clearSessionData(session: CrawlingSessionID)
    suspend fun getSessionStats(session: CrawlingSessionID): SessionStats
}
