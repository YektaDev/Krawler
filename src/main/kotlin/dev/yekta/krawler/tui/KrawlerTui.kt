package dev.yekta.krawler.tui

import dev.yekta.krawler.console.Ask
import dev.yekta.krawler.console.Ask.Option
import dev.yekta.krawler.console.info
import dev.yekta.krawler.console.option
import dev.yekta.krawler.model.Page.MAIN_MENU
import dev.yekta.krawler.model.Page.SETTINGS

class KrawlerTui(
    private val onStartCrawlingPress: () -> KrawlerStartResult,
    private val onExitPress: () -> Unit,
) {
    private val manager = TuiStateManager { page ->
        when (page) {
            MAIN_MENU -> TODO()
            SETTINGS -> TODO()
        }
    }

    fun start() = manager.bindScreen()

    private fun handleMenu(title: String, back: (() -> Unit)? = null, vararg options: Option) {
        val options = when {
            back != null -> options.toList().let { it + Option(it.size.toString(), "Back", back) }
            else -> options.toList()
        }
        Ask.optionsInSection(
            sectionTitle = title,
            options = options.toTypedArray(),
        ).action()
    }

    private fun mainMenu() {
        Ask.optionsInSection(
            sectionTitle = "Krawler",
            "Start Crawling" to {},
            "Settings" to { manager.navigate(SETTINGS) },
            "Exit" to {},
        ).action()
    }

    private fun settingsMenu(): Option {
        return Ask.optionsInSection(
            sectionTitle = "Settings",
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
            "Back" to {},
        )
    }

    private fun handleOnStart() = when (val result = onStartCrawlingPress()) {
        is KrawlerStartResult.Success -> info("Crawler started...")
        is KrawlerStartResult.Failure -> error("Failed to start crawling: ${result.reason}")
    }
}