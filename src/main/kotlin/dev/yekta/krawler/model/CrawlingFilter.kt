package dev.yekta.krawler.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface CrawlingFilter {
    data class Blacklist(val disallowPatterns: List<Regex>) : CrawlingFilter
    data class Whitelist(val allowPatterns: List<Regex>) : CrawlingFilter
}
