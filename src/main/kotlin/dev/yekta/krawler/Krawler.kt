package dev.yekta.krawler

import dev.yekta.krawler.domain.CrawlerImp
import dev.yekta.krawler.model.CrawlingFilter
import dev.yekta.krawler.model.KrawlerSettings
import dev.yekta.krawler.tui.KrawlerStartResult
import dev.yekta.krawler.tui.KrawlerTui
import kotlin.system.exitProcess

class Krawler {
    private val settings = KrawlerSettings(
        seeds = listOf(),
        filter = CrawlingFilter.Whitelist(listOf()),
    )
    private val crawler = CrawlerImp(settings)

    private val tui = KrawlerTui(
        onStartCrawlingPress = {
//            settings.load()
            crawler.start()
            KrawlerStartResult.Success
        },
        onExitPress = { exitProcess(0) },
    )

    fun start() = tui.start()
}
