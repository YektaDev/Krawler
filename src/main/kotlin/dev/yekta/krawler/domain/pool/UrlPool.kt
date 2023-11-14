package dev.yekta.krawler.domain.pool

import dev.yekta.krawler.domain.pool.model.UrlState

interface UrlPool {
    operator fun get(url: String): UrlState?
    operator fun set(url: String, state: UrlState)
}
