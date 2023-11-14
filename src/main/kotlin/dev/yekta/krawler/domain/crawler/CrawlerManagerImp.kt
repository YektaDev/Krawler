package dev.yekta.krawler.domain.crawler

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.model.KrawlerSettings
import dev.yekta.krawler.repo.Repo
import dev.yekta.krawler.repo.util.pause
import dev.yekta.krawler.repo.util.resume
import dev.yekta.krawler.repo.util.start
import dev.yekta.krawler.repo.util.stop
import kotlinx.coroutines.Job

class CrawlerManagerImp(
    private val sessionId: CrawlingSessionID,
    private val settings: KrawlerSettings,
    private val repo: Repo,
) : CrawlerManager {
    private val crawler: Crawler = CrawlerImp(sessionId, settings, repo)
    private var crawlingJob: Job? = null

    override fun start() {
        repo.activity.start(sessionId)
        crawlingJob = crawler.crawl(seeds = settings.seeds)
    }

    override fun stop() {
        repo.activity.stop(sessionId)
        crawlingJob?.cancel()
        crawlingJob = null
    }

    override fun pause() {
        repo.activity.pause(sessionId)
        TODO("Not yet implemented")
    }

    override fun resume() {
        repo.activity.resume(sessionId)
        TODO("Not yet implemented")
    }
}
