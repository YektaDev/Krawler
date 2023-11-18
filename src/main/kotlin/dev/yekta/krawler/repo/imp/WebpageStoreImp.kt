package dev.yekta.krawler.repo.imp

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.repo.WebpageStore
import dev.yekta.krawler.repo.orm.DBManager
import dev.yekta.krawler.repo.orm.table.Webpages
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class WebpageStoreImp : WebpageStore {
    override suspend fun add(session: CrawlingSessionID, epochSeconds: Long, url: String, html: String) {
        DBManager.transaction {
            Webpages.insert {
                it[this.sessionId] = session.value
                it[this.atEpochSeconds] = epochSeconds
                it[this.url] = url
                it[this.html] = html
            }
        }
    }

    override suspend fun total(session: CrawlingSessionID): Long = DBManager.transaction {
        Webpages.select { Webpages.sessionId eq session.value }.count()
    }

    override suspend fun averageHtmlLength(session: CrawlingSessionID): Double = DBManager.transaction {
        Webpages.select { Webpages.sessionId eq session.value }.map { it[Webpages.html].length }.average()
    }
}
