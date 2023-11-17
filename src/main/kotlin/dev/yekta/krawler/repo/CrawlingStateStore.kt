package dev.yekta.krawler.repo

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.model.StoredUrl

interface CrawlingStateStore {
    suspend fun maxPriority(session: CrawlingSessionID): StoredUrl?
    suspend fun minPriority(session: CrawlingSessionID): StoredUrl?

    suspend fun add(session: CrawlingSessionID, url: StoredUrl)
    suspend fun remove(session: CrawlingSessionID, url: String)
}
