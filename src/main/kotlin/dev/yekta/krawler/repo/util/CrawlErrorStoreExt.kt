package dev.yekta.krawler.repo.util

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.repo.CrawlErrorStore

suspend fun CrawlErrorStore.add(session: CrawlingSessionID, url: String, error: String) = add(
    session = session,
    epochSeconds = currentEpochSeconds(),
    url = url,
    error = error,
)
