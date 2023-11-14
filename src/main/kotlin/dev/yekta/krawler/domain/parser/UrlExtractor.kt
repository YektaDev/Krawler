package dev.yekta.krawler.domain.parser

import dev.yekta.krawler.model.CrawlingFilter

fun interface UrlExtractor {
    fun extract(url: String, html: String, filter: CrawlingFilter): List<String>
}
