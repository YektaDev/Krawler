package dev.yekta.krawler.repo.imp

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.repo.CrawlActivityStore
import dev.yekta.krawler.repo.orm.DBManager
import dev.yekta.krawler.repo.orm.table.CrawlActivities
import org.jetbrains.exposed.sql.insert

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
    override suspend fun addResume(session: CrawlingSessionID, epochSeconds: Long) = add(session, epochSeconds, "Resume")
}
