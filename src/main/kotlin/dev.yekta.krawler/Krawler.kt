package dev.yekta.krawler

import dev.yekta.krawler.domain.CrawlerImp
import dev.yekta.krawler.model.KrawlerSettings
import dev.yekta.krawler.tui.KrawlerStartResult
import dev.yekta.krawler.tui.KrawlerTui
import kotlin.system.exitProcess

class Krawler {
    private val settings = KrawlerSettings()
    private val crawler = CrawlerImp(settings)

    private val tui = KrawlerTui(
        onStartCrawlingPress = {
            crawler.start()
            KrawlerStartResult.Success
        },
        onExitPress = { exitProcess(0) },
    )

    fun start() = tui.start()
}