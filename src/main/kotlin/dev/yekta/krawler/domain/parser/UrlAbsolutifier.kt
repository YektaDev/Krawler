package dev.yekta.krawler.domain.parser

fun interface UrlAbsolutifier {
    fun extractRelUrlsAsAbsolute(url: String, html: String): List<String>
}
