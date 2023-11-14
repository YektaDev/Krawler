package dev.yekta.krawler.repo.util

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.repo.CrawlActivityStore
import kotlinx.datetime.Clock.System.now

fun CrawlActivityStore.start(session: CrawlingSessionID) = addStart(session, now().epochSeconds)
fun CrawlActivityStore.stop(session: CrawlingSessionID) = addStop(session, now().epochSeconds)
fun CrawlActivityStore.pause(session: CrawlingSessionID) = addPause(session, now().epochSeconds)
fun CrawlActivityStore.resume(session: CrawlingSessionID) = addResume(session, now().epochSeconds)
