package dev.yekta.krawler.domain.scheduler

import dev.yekta.krawler.domain.scheduler.model.ScheduledUrl
import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.model.StoredUrl
import dev.yekta.krawler.repo.CrawlingStateStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FifoScheduler(
    private val session: CrawlingSessionID,
    private val state: CrawlingStateStore,
) : Scheduler {
    private companion object {
        const val INITIAL_PRIORITY = Long.MAX_VALUE
    }

    private suspend fun getMinPriority() = state.minPriority(session)?.priority ?: INITIAL_PRIORITY

    override suspend fun schedule(url: ScheduledUrl) {
        val storedUrl = StoredUrl(
            url = url.url,
            depth = url.depth,
            session = session,
            priority = getMinPriority() - 1,
        )
        state.add(session, storedUrl)
    }

    private val nextMutex = Mutex()
    override suspend fun next(): ScheduledUrl? = nextMutex.withLock {
        val next = state.maxPriority(session) ?: return null
        state.remove(session, next.url)
        return ScheduledUrl(
            url = next.url,
            depth = next.depth,
        )
    }
}