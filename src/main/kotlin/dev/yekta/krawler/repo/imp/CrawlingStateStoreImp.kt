package dev.yekta.krawler.repo.imp

import dev.yekta.krawler.model.CrawlingSessionID
import dev.yekta.krawler.model.StoredUrl
import dev.yekta.krawler.repo.CrawlingStateStore
import dev.yekta.krawler.repo.orm.DBManager
import dev.yekta.krawler.repo.orm.table.CrawlingStates
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class CrawlingStateStoreImp : CrawlingStateStore {
    private suspend fun getFirstSortedByPriority(session: CrawlingSessionID, order: SortOrder): StoredUrl? {
        val resultRow = DBManager.transaction {
            CrawlingStates
                .select { CrawlingStates.sessionId eq session.value }
                .orderBy(CrawlingStates.priority, order)
                .limit(1)
                .singleOrNull()
        } ?: return null
        return with(resultRow) {
            StoredUrl(
                session = get(CrawlingStates.sessionId).let(::CrawlingSessionID),
                url = get(CrawlingStates.url),
                priority = get(CrawlingStates.priority),
                depth = get(CrawlingStates.depth),
            )
        }
    }

    override suspend fun maxPriority(session: CrawlingSessionID): StoredUrl? =
        getFirstSortedByPriority(session, SortOrder.DESC)

    override suspend fun minPriority(session: CrawlingSessionID): StoredUrl? =
        getFirstSortedByPriority(session, SortOrder.ASC)

    override suspend fun add(session: CrawlingSessionID, url: StoredUrl) {
        DBManager.transaction {
            CrawlingStates.insert {
                it[this.sessionId] = session.value
                it[this.url] = url.url
                it[this.priority] = url.priority
                it[this.depth] = url.depth
            }
        }
    }

    override suspend fun remove(session: CrawlingSessionID, url: String) {
        DBManager.transaction {
            CrawlingStates.deleteWhere {
                (this.sessionId eq session.value) and (this.url eq url)
            }
        }
    }
}
