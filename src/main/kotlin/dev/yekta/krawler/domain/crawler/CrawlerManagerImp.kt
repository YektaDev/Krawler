package dev.yekta.krawler.domain.crawler

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.model.KrawlerSettings
import dev.yekta.krawler.repo.Repo
import dev.yekta.krawler.repo.util.pause
import dev.yekta.krawler.repo.util.resume
import dev.yekta.krawler.repo.util.start
import dev.yekta.krawler.repo.util.stop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CrawlerManagerImp(
    private val sessionId: CrawlingSessionID,
    private val settings: KrawlerSettings,
    private val repo: Repo,
) : CrawlerManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val crawler: Crawler = CrawlerImp(sessionId, settings, repo, scope)
    private var crawlingJob: Job? = null

    override fun start() {
        scope.launch {
            repo.activity.start(sessionId)
        }
        crawlingJob = crawler.crawl(seeds = settings.seeds)
    }

    override fun stop() {
        scope.launch {
            repo.activity.stop(sessionId)
        }
        crawlingJob?.cancel()
        crawlingJob = null
    }

    override fun pause() {
        scope.launch {
            repo.activity.pause(sessionId)
        }
        // TODO: TBD
    }

    override fun resume() {
        scope.launch {
            repo.activity.resume(sessionId)
        }
        // TODO: TBD
    }
}
