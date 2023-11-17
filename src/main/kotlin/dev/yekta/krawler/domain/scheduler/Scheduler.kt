package dev.yekta.krawler.domain.scheduler

import dev.yekta.krawler.domain.scheduler.model.ScheduledUrl

interface Scheduler {
    suspend fun schedule(url: ScheduledUrl)
    suspend fun next(): ScheduledUrl?
}
