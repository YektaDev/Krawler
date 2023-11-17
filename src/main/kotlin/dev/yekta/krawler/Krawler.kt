package dev.yekta.krawler

import dev.yekta.krawler.domain.crawler.CrawlerManager
import dev.yekta.krawler.domain.crawler.CrawlerManagerImp
import dev.yekta.krawler.log.Log
import dev.yekta.krawler.model.CrawlingFilter
import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.model.KrawlerSettings
import dev.yekta.krawler.repo.RepoImp
import dev.yekta.krawler.repo.imp.CrawlActivityStoreImp
import dev.yekta.krawler.repo.imp.CrawlErrorStoreImp
import dev.yekta.krawler.repo.imp.CrawlingStateStoreImp
import dev.yekta.krawler.repo.imp.WebpageStoreImp
import dev.yekta.krawler.tui.KrawlerStartResult
import dev.yekta.krawler.tui.KrawlerTui
import kotlinx.coroutines.runBlocking

class Krawler {
    private val settings = KrawlerSettings(
        seeds = listOf(),
        filter = CrawlingFilter.Whitelist(listOf()),
    ).also { Log.verbose = it.verbose }

    private val repo by lazy {
        RepoImp(
            state = CrawlingStateStoreImp(),
            webpage = WebpageStoreImp(),
            activity = CrawlActivityStoreImp(),
            error = CrawlErrorStoreImp(),
        )
    }

    private val manager: CrawlerManager = CrawlerManagerImp(
        sessionId = CrawlingSessionID(""),
        settings = settings,
        repo = repo,
    )

    private val tui = KrawlerTui(
        getSessions = {
            runBlocking {
                repo.getSessions()
            }
        },
        removeSession = { session ->
            runBlocking {
                repo.clearSessionData(session)
            }
        },
        startSession = { session ->
            manager.start()
            KrawlerStartResult.Success
        },
    )

    fun start() = tui.start()
}
