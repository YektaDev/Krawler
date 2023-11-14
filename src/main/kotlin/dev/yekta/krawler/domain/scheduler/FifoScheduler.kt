package dev.yekta.krawler.domain.scheduler

import dev.yekta.krawler.domain.scheduler.model.ScheduledUrl
import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.model.StoredUrl
import dev.yekta.krawler.repo.CrawlingStateStore

class FifoScheduler(
    private val session: CrawlingSessionID,
    private val state: CrawlingStateStore,
) : Scheduler {
    private companion object {
        const val INITIAL_PRIORITY = Long.MAX_VALUE
    }

    private val minPriority get() = state.minPriority(session)?.priority ?: INITIAL_PRIORITY

    override fun schedule(url: ScheduledUrl) {
        val storedUrl = StoredUrl(
            url = url.url,
            depth = url.depth,
            session = session,
            priority = minPriority - 1,
        )
        state.add(storedUrl)
    }

    override fun next(): ScheduledUrl? {
        val next = state.maxPriority(session) ?: return null
        return ScheduledUrl(
            url = next.url,
            depth = next.depth,
        )
    }
}