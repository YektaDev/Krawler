package dev.yekta.krawler.repo.imp

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.repo.CrawlErrorStore
import dev.yekta.krawler.repo.orm.DBManager
import dev.yekta.krawler.repo.orm.table.CrawlErrors
import org.jetbrains.exposed.sql.insert

class CrawlErrorStoreImp : CrawlErrorStore {
    override suspend fun add(session: CrawlingSessionID, epochSeconds: Long, url: String, error: String) {
        DBManager.transaction {
            CrawlErrors.insert {
                it[this.sessionId] = session.value
                it[this.atEpochSeconds] = epochSeconds
                it[this.url] = url
                it[this.error] = error
            }
        }
    }
}
