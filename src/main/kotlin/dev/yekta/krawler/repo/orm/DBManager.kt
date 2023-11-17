package dev.yekta.krawler.repo.orm

import dev.yekta.krawler.repo.orm.table.CrawlActivities
import dev.yekta.krawler.repo.orm.table.CrawlErrors
import dev.yekta.krawler.repo.orm.table.CrawlingStates
import dev.yekta.krawler.repo.orm.table.Webpages
import dev.yekta.krawler.repo.orm.util.StdOutLogger
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DBManager {
    private const val DB_PATH = "KrawlerData.db"
    private val db = Database.connect("jdbc:sqlite:${DB_PATH}", "org.sqlite.JDBC")
    private val tables = arrayOf(
        CrawlActivities,
        CrawlingStates,
        CrawlErrors,
        Webpages,
    )

    init {
        org.jetbrains.exposed.sql.transactions.transaction {
            addLogger(StdOutLogger)
            tables.forEach { SchemaUtils.create(it) }
        }
    }

    suspend fun <T> transaction(statement: suspend Transaction.() -> T) =
        newSuspendedTransaction(Dispatchers.IO, db = db, statement = statement)
}
