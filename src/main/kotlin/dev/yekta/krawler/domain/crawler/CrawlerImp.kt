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
import dev.yekta.krawler.repo.util.currentEpochSeconds
import kotlinx.coroutines.*
import okio.utf8Size
import kotlin.math.min
import kotlin.math.roundToLong

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

    private suspend fun reachedMaxPages() = settings.maxPages.let { it != null && repo.webpage.total(sessionId) > it }
    private suspend fun run() {
        var url = scheduler.next()
        while (url != null) {
            val u = url
            fetcher.fetch(u.url) { result -> handleFetchResult(u.depth, u.url, result) }

            if (reachedMaxPages()) break
            url = scheduler.next()
            waitForUrl(isUrlEmpty = { url == null }, trySetUrl = { url = scheduler.next() })
        }

        scope.launch(NonCancellable) {
            finish()

            // Ideally we should cancel the remaining crawling scopes, but I don't have enough time for
            // a task of this priority at the time of writing!
            info("The process is over. Waiting for possible active calls to finish...")
            delay(safeTimeoutMs())

            informEndOfCrawling(reachedMaxPageLimit = url != null)
        }
        scope.coroutineContext.cancelChildren()
    }

    private fun safeTimeoutMs(): Long = ((settings.connectTimeoutMs + settings.readTimeoutMs) * 1.1f).roundToLong()

    private suspend fun waitForUrl(isUrlEmpty: () -> Boolean, trySetUrl: suspend () -> Unit) {
        val startSec = currentEpochSeconds()
        val timeoutSec = safeTimeoutMs() / 1000
        while (isUrlEmpty() && currentEpochSeconds() - startSec <= timeoutSec) {
            info("Waiting for active crawling processes to provide new URLs...")
            delay(1000L)
            trySetUrl()
        }
    }

    private suspend fun informEndOfCrawling(reachedMaxPageLimit: Boolean) {
        when (reachedMaxPageLimit) {
            true -> info("Crawling reached its max page count limit.")
            false -> info("All discovered URLs are processed. ")
        }
        info("The crawling process is over.")
        info("Total Crawled URLs: ${repo.webpage.total(sessionId)}")
    }

    private suspend fun handleFetchResult(depth: Int, url: String, result: FetchResult) {
        val minDepth = updateAndGetMinDepth(url, depth)
        if (reachedMaxPages()) return
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
            if (reachedMaxPages()) return
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
