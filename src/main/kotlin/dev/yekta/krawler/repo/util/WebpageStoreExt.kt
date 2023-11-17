package dev.yekta.krawler.repo.util

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.repo.WebpageStore

suspend fun WebpageStore.add(session: CrawlingSessionID, url: String, html: String) = add(
    session = session,
    epochSeconds = currentEpochSeconds(),
    url = url,
    html = html,
)
