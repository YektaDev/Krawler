package dev.yekta.krawler.repo.util

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.repo.CrawlActivityStore

suspend fun CrawlActivityStore.start(session: CrawlingSessionID) = addStart(session, currentEpochSeconds())
suspend fun CrawlActivityStore.stop(session: CrawlingSessionID) = addStop(session, currentEpochSeconds())
suspend fun CrawlActivityStore.pause(session: CrawlingSessionID) = addPause(session, currentEpochSeconds())
suspend fun CrawlActivityStore.resume(session: CrawlingSessionID) = addResume(session, currentEpochSeconds())
