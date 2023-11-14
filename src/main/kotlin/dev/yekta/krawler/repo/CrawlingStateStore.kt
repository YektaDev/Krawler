package dev.yekta.krawler.repo

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.model.StoredUrl

interface CrawlingStateStore {
    fun maxPriority(session: CrawlingSessionID): StoredUrl?
    fun minPriority(session: CrawlingSessionID): StoredUrl?

    fun add(url: StoredUrl)
    fun remove(session: CrawlingSessionID, url: String)
}
