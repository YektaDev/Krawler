package dev.yekta.krawler.tui

import dev.yekta.krawler.console.Ask
import dev.yekta.krawler.console.Ask.Option
import dev.yekta.krawler.console.error
import dev.yekta.krawler.console.info
import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.model.Page.*
import dev.yekta.krawler.repo.orm.util.SESSION_ID_MAX_LEN
import dev.yekta.krawler.tui.model.Menu
import kotlin.system.exitProcess

class KrawlerTui(
    private val getSessions: () -> List<CrawlingSessionID>,
    private val removeSession: (CrawlingSessionID) -> Unit,
    private val startSession: (CrawlingSessionID) -> KrawlerStartResult,
) {
    private val nav = TuiStackNavigatorImp { page ->
        when (page) {
            MAIN_MENU -> mainMenu()
            SETTINGS -> settingsMenu()
            SESSION_SELECTION -> sessionMenu()
            SESSION_DELETION -> removeMenu()
        }
    }

    fun start() = nav.bindTui()

    private fun mainMenu(): Menu = Menu(
        title = "Krawler",
        options = Option.listOf(
            "Let's Krawl!" to { nav.push(SESSION_SELECTION) },
            "Settings" to { nav.push(SETTINGS) },
            "Exit" to { exitProcess(0) },
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

    private fun sessionMenu(): Menu = Menu(
        title = "Crawling Sessions",
        options = buildOptionList {
            val sessions = getSessions()
            sessions.forEach { session ->
                add("Resume Session: ${session.value}" to { handleStartResult(startSession(session)) })
            }
            if (sessions.isNotEmpty()) {
                add("Remove one or more sessions." to { nav.push(SESSION_DELETION) })
            }

            add("Create & Start a new crawling session." to { handleStartResult(startSession(createNewSession())) })
        }
    )

    private fun createNewSession(): CrawlingSessionID =
        Ask.string("Enter a unique session name (maximum characters: $SESSION_ID_MAX_LEN):").let { id ->
            when {
                id.length > SESSION_ID_MAX_LEN -> {
                    error(
                        "Max length exceeded. It must be at most $SESSION_ID_MAX_LEN" +
                                " characters long, but it was ${id.length} characters)."
                    )
                    createNewSession()
                }

                else -> CrawlingSessionID(id)
            }
        }

    private fun removeMenu(): Menu = Menu(
        title = "Remove Sessions",
        options = buildOptionList {
            getSessions().forEach { session ->
                add(
                    "Remove: ${session.value}" to {
                        removeSession(session)
                        info("Session ${session.value} removed!")
                        if (getSessions().isEmpty()) nav.pop()
                    }
                )
            }
        }
    )

    private fun handleStartResult(result: KrawlerStartResult) = when (result) {
        is KrawlerStartResult.Success -> info("Crawling session started...")
        is KrawlerStartResult.Failure -> error("Failed to start crawling: ${result.reason}")
    }

    private inline fun buildOptionList(
        builder: MutableList<Pair<String, () -> Unit>>.() -> Unit,
    ) = Option.listOf(
        *buildList { builder() }.toTypedArray()
    )
}