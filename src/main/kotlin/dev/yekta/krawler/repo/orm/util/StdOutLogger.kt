package dev.yekta.krawler.repo.orm.util

import dev.yekta.krawler.console.verbose
import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs

object StdOutLogger : SqlLogger {
    override fun log(context: StatementContext, transaction: Transaction) {
        verbose("[SQL] ${context.expandArgs(transaction)}")
    }
}
