package dev.yekta.krawler.repo

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.model.SessionStats
import dev.yekta.krawler.repo.orm.DBManager
import dev.yekta.krawler.repo.orm.table.CrawlActivities
import dev.yekta.krawler.repo.orm.table.CrawlErrors
import dev.yekta.krawler.repo.orm.table.CrawlingStates
import dev.yekta.krawler.repo.orm.table.Webpages
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

data class RepoImp(
    override val state: CrawlingStateStore,
    override val webpage: WebpageStore,
    override val activity: CrawlActivityStore,
    override val error: CrawlErrorStore,
) : Repo {
    override suspend fun getSessions(): List<CrawlingSessionID> {
        val tableToId = arrayOf(
            CrawlingStates to CrawlingStates.sessionId,
            Webpages to Webpages.sessionId,
            CrawlActivities to CrawlActivities.sessionId,
            CrawlErrors to CrawlErrors.sessionId,
        )

        return tableToId.flatMap { (table, row) ->
            DBManager.transaction {
                table
                    .selectAll()
                    .groupBy { it[row] }
                    .map { it.key.let(::CrawlingSessionID) }
            }
        }.distinct()
    }

    override suspend fun clearSessionData(session: CrawlingSessionID) {
        val sessionId = session.value
        DBManager.transaction { CrawlingStates.deleteWhere { this.sessionId eq sessionId } }
        DBManager.transaction { Webpages.deleteWhere { this.sessionId eq sessionId } }
        DBManager.transaction { CrawlActivities.deleteWhere { this.sessionId eq sessionId } }
        DBManager.transaction { CrawlErrors.deleteWhere { this.sessionId eq sessionId } }
    }

    override suspend fun getSessionStats(session: CrawlingSessionID): SessionStats = SessionStats(
        id = session,
        totalSeconds = activity.sessionDurationSeconds(session),
        totalCrawledPages = webpage.total(session),
        averageHtmlLength = webpage.averageHtmlLength(session),
        urlsInQueue = DBManager.transaction {
            CrawlingStates.select { CrawlingStates.sessionId eq session.value }.count()
        }
    )
}