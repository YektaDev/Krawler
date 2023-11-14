package dev.yekta.krawler.domain.crawler

import kotlinx.coroutines.Job

interface Crawler {
    fun crawl(seeds: List<String>): Job
}
