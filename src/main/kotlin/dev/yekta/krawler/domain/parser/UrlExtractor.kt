package dev.yekta.krawler.domain.parser

import dev.yekta.krawler.model.CrawlingFilter

interface UrlExtractor {
    val filter: CrawlingFilter

    fun extract(html: String): List<String>
}