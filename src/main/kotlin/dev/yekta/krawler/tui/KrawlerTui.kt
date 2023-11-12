package dev.yekta.krawler.tui

import dev.yekta.krawler.console.Ask.Option
import dev.yekta.krawler.console.info
import dev.yekta.krawler.model.Page.MAIN_MENU
import dev.yekta.krawler.model.Page.SETTINGS
import dev.yekta.krawler.tui.model.Menu

class KrawlerTui(
    private val onStartCrawlingPress: () -> KrawlerStartResult,
    private val onExitPress: () -> Unit,
) {
    private val nav = TuiStackNavigatorImp { page ->
        when (page) {
            MAIN_MENU -> mainMenu()
            SETTINGS -> settingsMenu()
        }
    }

    fun start() = nav.bindTui()

    private fun mainMenu(): Menu = Menu(
        title = "Krawler",
        options = Option.listOf(
            "Start Crawling" to {},
            "Settings" to { nav.push(SETTINGS) },
            "Exit" to {},
        )
    )

    private fun settingsMenu(): Menu = Menu(
        title = "Settings",
        options = Option.listOf(
            "URL" to {},
            "Depth" to {},
            "Max Pages" to {},
            "Max Duration" to {},
            "Max Page Size" to {},
            "User Agent" to {},
            "Concurrent Connections" to {},
            "Output Directory" to {},
            "Output Format" to {},
            "Verbose" to {},
        ),
    )

    private fun handleOnStart() = when (val result = onStartCrawlingPress()) {
        is KrawlerStartResult.Success -> info("Crawler started...")
        is KrawlerStartResult.Failure -> error("Failed to start crawling: ${result.reason}")
    }
}