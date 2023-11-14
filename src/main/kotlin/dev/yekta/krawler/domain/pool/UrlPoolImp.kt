package dev.yekta.krawler.domain.pool

import dev.yekta.krawler.domain.pool.model.UrlState
import java.util.concurrent.ConcurrentHashMap

class UrlPoolImp : UrlPool {
    private val pool = ConcurrentHashMap<String, UrlState>()

    override operator fun get(url: String): UrlState? = pool[url]
    override operator fun set(url: String, state: UrlState) = pool.set(url, state)
}
