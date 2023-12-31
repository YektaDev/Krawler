package dev.yekta.krawler

import dev.yekta.krawler.domain.crawler.CrawlerManager
import dev.yekta.krawler.domain.crawler.CrawlerManagerImp
import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.repo.RepoImp
import dev.yekta.krawler.repo.imp.CrawlActivityStoreImp
import dev.yekta.krawler.repo.imp.CrawlErrorStoreImp
import dev.yekta.krawler.repo.imp.CrawlingStateStoreImp
import dev.yekta.krawler.repo.imp.WebpageStoreImp
import dev.yekta.krawler.settings.SettingsFile
import dev.yekta.krawler.tui.KrawlerStartResult
import dev.yekta.krawler.tui.KrawlerTui
import kotlinx.coroutines.runBlocking

class Krawler {
    private val repo by lazy {
        RepoImp(
            state = CrawlingStateStoreImp(),
            webpage = WebpageStoreImp(),
            activity = CrawlActivityStoreImp(),
            error = CrawlErrorStoreImp(),
        )
    }

    private val onCrawlingFinished: (CrawlingSessionID) -> Unit = { session ->
        tui.displayStats(session = session)
        tui.navigateToMainMenu()
    }

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
        isSessionComplete = { session ->
            runBlocking {
                repo.activity.isCompleted(session)
            }
        },
        getSessionStats = { session ->
            runBlocking {
                repo.getSessionStats(session)
            }
        },
        startSession = { session ->
            runCatching(SettingsFile::read).fold(
                onFailure = { KrawlerStartResult.Failure(it.message ?: it.stackTraceToString()) },
                onSuccess = { settings ->
                    val manager: CrawlerManager = CrawlerManagerImp(
                        sessionId = session,
                        settings = settings,
                        repo = repo,
                        finish = { onCrawlingFinished(session) },
                    )
                    manager.start()
                    KrawlerStartResult.Success
                },
            )
        },
    )

    fun start() = tui.start()
}
