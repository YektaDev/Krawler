package dev.yekta.krawler.repo.orm.table

import dev.yekta.krawler.repo.orm.util.SESSION_ID_KEY
import dev.yekta.krawler.repo.orm.util.SESSION_ID_MAX_LEN
import org.jetbrains.exposed.dao.id.IntIdTable

object CrawlActivities : IntIdTable() {
    val sessionId = varchar(SESSION_ID_KEY, SESSION_ID_MAX_LEN)
    val atEpochSeconds = long("atEpochSeconds")
    val type = varchar("type", 50)
}
