package dev.yekta.krawler.domain.crawler

import dev.yekta.krawler.console.info
import dev.yekta.krawler.console.verbose
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
import dev.yekta.krawler.repo.util.add
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import okio.utf8Size
import kotlin.math.min

class CrawlerImp(
    private val sessionId: CrawlingSessionID,
    private val settings: KrawlerSettings,
    private val repo: Repo,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val finish: () -> Unit,
) : Crawler {
    private val pool: UrlPool = UrlPoolImp()
    private val scheduler: Scheduler = FifoScheduler(sessionId, repo.state)
    private val fetcher: ConcurrentFetcher = ConcurrentFetcherImp(
        maxConnections = settings.concurrentConnections,
        shouldFollowRedirects = settings.shouldFollowRedirects,
        userAgent = settings.userAgent,
        connectTimeoutMs = settings.connectTimeoutMs,
        readTimeoutMs = settings.readTimeoutMs,
        retriesOnServerError = settings.retriesOnServerError,
        customHeaders = settings.customHeaders,
    )
    private val extractor: UrlExtractor = UrlExtractorImp()

    override fun crawl(seeds: List<String>) = scope.launch {
        settings.seeds.forEach { seed ->
            val url = ScheduledUrl(url = seed, depth = 1)
            scheduler.schedule(url)
        }

        run()
    }

    private suspend fun run() {
        var i = 0
        val hasReachedPageLimit = {
            when (val max = settings.maxPages) {
                null -> false
                else -> ++i >= max
            }
        }

        var url = scheduler.next()
        while (url != null) {
            val u = url
            fetcher.fetch(u.url) { result -> handleFetchResult(u.depth, u.url, result) }

            if (hasReachedPageLimit()) break
            url = scheduler.next()
            val start = Clock.System.now().epochSeconds
            while (url == null && Clock.System.now().epochSeconds - start < settings.connectTimeoutMs + settings.readTimeoutMs) {
                info("Waiting for active crawling processes to provide new URLs...")
                delay(1000)
                url = scheduler.next()
            }
        }

        informEndOfCrawling(reachedMaxPageLimit = url != null, totalCrawled = i)
        finish()
    }

    private fun informEndOfCrawling(reachedMaxPageLimit: Boolean, totalCrawled: Int) {
        when (reachedMaxPageLimit) {
            true -> info("Crawling reached its max page count limit.")
            false -> info("All discovered URLs are processed. ")
        }
        info("The crawling process is over.")
        info("Total Crawled URLs: $totalCrawled")
    }

    private suspend fun handleFetchResult(depth: Int, url: String, result: FetchResult) {
        val minDepth = updateAndGetMinDepth(url, depth)
        when (result) {
            is Html -> {
                if (settings.maxPageSizeBytes != null && result.html.utf8Size() > settings.maxPageSizeBytes) {
                    info("[$url] has ${result.html.utf8Size()} bytes, skipping it.")
                    return
                }

                repo.webpage.add(sessionId, url, result.html)

                if (settings.depth <= minDepth) {
                    verbose("[$url] URL is too deep from seeds to be used to extract new links, skipping link extraction.")
                    return
                }
                verbose("[$url] Extracting URLs")

                scheduleNewUrls(
                    depth = minDepth + 1,
                    urls = extractor.extract(
                        url = url,
                        html = result.html,
                        filter = settings.filter,
                    ),
                )
            }

            is NotHtml -> pool[url] = UrlState.Visited.NonHtml(minDepth)

            is ReadError -> {
                repo.error.add(sessionId, url, result.message)
                pool[url] = UrlState.ReadError(minDepth)
            }
        }
    }

    private suspend fun scheduleNewUrls(depth: Int, urls: List<String>) {
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
