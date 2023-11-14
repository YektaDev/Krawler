package dev.yekta.krawler.log

import dev.yekta.krawler.console.error
import dev.yekta.krawler.console.info
import dev.yekta.krawler.console.verbose
import kotlin.properties.Delegates

object Log {
    var verbose by Delegates.notNull<Boolean>()

    fun i(message: String) = info(message)
    fun e(message: String) = error(message)
    fun v(message: String) = if (verbose) verbose(message) else Unit
}