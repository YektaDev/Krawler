package dev.yekta.krawler.repo.imp

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.repo.CrawlActivityStore
import dev.yekta.krawler.repo.orm.DBManager
import dev.yekta.krawler.repo.orm.table.CrawlActivities
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class CrawlActivityStoreImp : CrawlActivityStore {
    private suspend fun add(session: CrawlingSessionID, epochSeconds: Long, event: String) {
        DBManager.transaction {
            CrawlActivities.insert {
                it[this.sessionId] = session.value
                it[this.atEpochSeconds] = epochSeconds
                it[this.type] = event
            }
        }
    }

    override suspend fun addStart(session: CrawlingSessionID, epochSeconds: Long) = add(session, epochSeconds, "Start")
    override suspend fun addStop(session: CrawlingSessionID, epochSeconds: Long) = add(session, epochSeconds, "Stop")
    override suspend fun addPause(session: CrawlingSessionID, epochSeconds: Long) = add(session, epochSeconds, "Pause")
    override suspend fun addResume(session: CrawlingSessionID, epochSeconds: Long) =
        add(session, epochSeconds, "Resume")

    override suspend fun isCompleted(session: CrawlingSessionID): Boolean = DBManager.transaction {
        CrawlActivities
            .select { (CrawlActivities.sessionId eq session.value) and (CrawlActivities.type eq "Stop") }
            .singleOrNull() != null
    }

    override suspend fun sessionDurationSeconds(session: CrawlingSessionID): Long = DBManager.transaction {
        val times = CrawlActivities
            .select { CrawlActivities.sessionId eq session.value }
            .asSequence()
            .map { it[CrawlActivities.type] to it[CrawlActivities.atEpochSeconds] }
            .onEach {
                require(it.first == "Start" || it.first == "Stop") { "Pause and Resume are not implemented yet!" }
            }
            .sortedBy { it.second }
            .map { it.second }
            .toList()
            .also {
                require(it.size <= 2) { "Too many records! This is unexpected!" }
                require(it.size >= 2) { "sessionDurationSeconds() cannot be called on unfinished sessions!" }
            }
        times[1] - times[0]
    }
}
