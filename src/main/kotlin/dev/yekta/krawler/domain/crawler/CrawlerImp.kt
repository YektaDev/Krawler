package dev.yekta.krawler.domain.crawler

import dev.yekta.krawler.domain.fetcher.ConcurrentFetcher
import dev.yekta.krawler.domain.fetcher.ConcurrentFetcherImp
import dev.yekta.krawler.domain.fetcher.FetchResult
import dev.yekta.krawler.domain.fetcher.FetchResult.*
import dev.yekta.krawler.domain.parser.UrlExtractor
import dev.yekta.krawler.domain.parser.UrlExtractorImp
import dev.yekta.krawler.domain.pool.UrlPool
import dev.yekta.krawler.domain.pool.UrlPoolImp
import dev.yekta.krawler.domain.pool.model.UrlState
import dev.yekta.krawler.domain.scheduler.FifoScheduler
import dev.yekta.krawler.domain.scheduler.Scheduler
import dev.yekta.krawler.domain.scheduler.model.ScheduledUrl
import dev.yekta.krawler.log.Log
import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.model.KrawlerSettings
import dev.yekta.krawler.repo.Repo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.min

class CrawlerImp(
    private val sessionId: CrawlingSessionID,
    private val settings: KrawlerSettings,
    private val repo: Repo,
) : Crawler {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val pool: UrlPool = UrlPoolImp()
    private val scheduler: Scheduler = FifoScheduler(sessionId, repo.crawlingState)
    private val fetcher: ConcurrentFetcher = ConcurrentFetcherImp(settings.concurrentConnections)
    private val extractor: UrlExtractor = UrlExtractorImp(settings.filter)

    override fun crawl(seeds: List<String>) = scope.launch {
        settings.seeds.forEach { seed ->
            val url = ScheduledUrl(url = seed, depth = 1)
            scheduler.schedule(url)
        }

        run()
    }

    private suspend fun run() {
        var url = scheduler.next()
        while (url != null) {
            val u = url
            fetcher.fetch(u.url) { result -> handleFetchResult(u.depth, u.url, result) }
            url = scheduler.next()
        }
    }

    private fun handleFetchResult(depth: Int, url: String, result: FetchResult) {
        val minDepth = updateAndGetMinDepth(url, depth)
        when (result) {
            is Html -> {
                repo.webpage.add(sessionId, url, result.html)
                scheduleNewUrls(
                    depth = minDepth + 1,
                    urls = extractor.extract(result.html),
                )
            }

            is NotHtml -> pool[url] = UrlState.Visited.NonHtml(minDepth)

            is ReadError -> {
                repo.error.add(sessionId, url, result.message)
                pool[url] = UrlState.ReadError(minDepth)
            }
        }
    }

    private fun scheduleNewUrls(depth: Int, urls: List<String>) {
        for (url in urls) {
            val minDepth = updateAndGetMinDepth(url, depth)
            if (pool[url] != null) continue

            Log.v("Discovered: $url")
            pool[url] = UrlState.NotVisited(minDepth)
            scheduler.schedule(ScheduledUrl(url, minDepth))
        }
    }

    private fun updateAndGetMinDepth(url: String, thisDepth: Int): Int {
        val thisUrl = pool[url] ?: return thisDepth

        val minDepth = min(thisUrl.minDepth, thisDepth)
        pool[url] = thisUrl.copy(minDepth)
        return minDepth
    }
}
