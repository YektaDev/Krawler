package dev.yekta.krawler.repo

import dev.yekta.krawler.model.CrawlingSessionID

interface Repo {
    val state: CrawlingStateStore
    val webpage: WebpageStore
    val activity: CrawlActivityStore
    val error: CrawlErrorStore

    suspend fun getSessions(): List<CrawlingSessionID>
    suspend fun clearSessionData(session: CrawlingSessionID)
}
