package dev.yekta.krawler.tui

import dev.yekta.krawler.console.*
import dev.yekta.krawler.console.Ask.Option
import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.model.Page.*
import dev.yekta.krawler.model.SessionStats
import dev.yekta.krawler.repo.orm.util.SESSION_ID_MAX_LEN
import dev.yekta.krawler.tui.model.Menu
import java.util.*
import kotlin.system.exitProcess

class KrawlerTui(
    private val getSessions: () -> List<CrawlingSessionID>,
    private val removeSession: (CrawlingSessionID) -> Unit,
    private val startSession: (CrawlingSessionID) -> KrawlerStartResult,
    private val isSessionComplete: (CrawlingSessionID) -> Boolean,
    private val getSessionStats: (CrawlingSessionID) -> SessionStats,
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

    fun navigateToMainMenu() {
        while (nav.hasBack) nav.pop()
    }

    fun displayStats(session: CrawlingSessionID) {
        val stats = getSessionStats(session)
        fun StringBuilder.stat(block: SessionStats.() -> Pair<String, String>) {
            val (key, value) = with(stats) { block() }
            append("".purpleBoldBright())
            append(key.padEnd(20))
            append(": ".whiteBold())
            append(value.yellowUnderlined().yellowBoldBright().reset())
            append('\n')
        }

        val statsString = buildString {
            stat { "ID" to id.value }
            stat {
                val mins = totalSeconds / 60
                val minsSec = totalSeconds % 60
                val minsString = if (minsSec > 0) "$mins Minutes and $minsSec Seconds" else "$mins Minutes"
                val duration = "$totalSeconds Seconds".let { if (mins > 0) "$it ($minsString)" else it }
                "Total Duration" to duration
            }
            stat { "Total Crawled Pages" to totalCrawledPages.toString() }
            stat { "Average HTML Length" to "%.2f".format(averageHtmlLength, Locale.ENGLISH) }
            stat { "URLs in Final Queue" to urlsInQueue.toString() }
        }
        info("Session Stats:\n")
        info(statsString)
    }

    private fun mainMenu(): Menu = Menu(
        title = "Krawler",
        options = Option.listOf(
            "Let's Krawl!" to { nav.push(SESSION_SELECTION) },
            "Settings" to { nav.push(SETTINGS) },
            "About" to {
                println(
                    "<[ Krawler ".whiteBoldBright() + "0.1.0".whiteBold() + " ]>\nBy ".whiteBoldBright() +
                            "Ali Khaleqi Yekta".purpleBoldBright() + " | ".whiteBold() +
                            "Me@yekta.dev".yellowUnderlined().reset()
                )
            },
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
            val finishedSessions = mutableListOf<CrawlingSessionID>()
            sessions.forEach { session ->
                if (isSessionComplete(session)) finishedSessions.add(session)
                else add("Resume Session: ${session.value}" to {
                    error("Sorry, resuming is not fully implemented yet.")
                    // handleStartResult(startSession(session))
                }
                )
            }
            finishedSessions.forEach { session ->
                add("View Session Result: ${session.value}" to { displayStats(session) })
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