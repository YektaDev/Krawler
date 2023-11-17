package dev.yekta.krawler.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed interface CrawlingFilter {
    @Serializable
    data class Blacklist(val disallowPatterns: List<String>) : CrawlingFilter {
        @Transient
        val disallowRegexPatterns = disallowPatterns.map(::Regex)
    }

    @Serializable
    data class Whitelist(val allowPatterns: List<String>) : CrawlingFilter {
        @Transient
        val allowRegexPatterns = allowPatterns.map(::Regex)
    }
}
