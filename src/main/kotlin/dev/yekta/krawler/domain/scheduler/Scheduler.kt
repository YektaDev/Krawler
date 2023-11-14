package dev.yekta.krawler.domain.scheduler

import dev.yekta.krawler.domain.scheduler.model.ScheduledUrl

interface Scheduler {
    fun schedule(url: ScheduledUrl)
    fun next(): ScheduledUrl?
}
