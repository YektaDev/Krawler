package dev.yekta.krawler.domain.parser

import dev.yekta.krawler.domain.parser.ValidUrlData.validChars
import dev.yekta.krawler.domain.parser.ValidUrlData.validPrefixes
import dev.yekta.krawler.model.CrawlingFilter
import dev.yekta.krawler.model.CrawlingFilter.Blacklist
import dev.yekta.krawler.model.CrawlingFilter.Whitelist

class UrlExtractorImp : UrlExtractor {
    private val absolutifier: UrlAbsolutifier = UrlAbsolutifierImp()

    private fun relUrlsAsAbs(url: String, html: String) = absolutifier.extractRelUrlsAsAbsolute(url = url, html = html)
    private fun absUrls(html: String): List<String> {
        val urls = mutableListOf<String>()
        var firstUncheckedPrefixIndex = 0
        var firstUncheckedPossibleEnd: Int

        while (true) {
            firstUncheckedPrefixIndex = validPrefixes.minOf { prefix ->
                prefix.indexOf(prefix, startIndex = firstUncheckedPrefixIndex)
            }
            if (firstUncheckedPrefixIndex !in 0..<html.lastIndex) break

            firstUncheckedPossibleEnd = html
                .substring(firstUncheckedPrefixIndex)
                .indexOfFirst { validChars.binarySearch(it) < 0 }
                // Means not finding any possible ending until the end of the string:
                .takeIf { it > firstUncheckedPrefixIndex } ?: html.lastIndex

            val possibleUrl = html.substring(firstUncheckedPrefixIndex, firstUncheckedPossibleEnd)
            urls.add(possibleUrl)

            firstUncheckedPrefixIndex = firstUncheckedPossibleEnd + 1
        }

        return urls
    }

    override fun extract(url: String, html: String, filter: CrawlingFilter): List<String> {
        val extractedUrls = (absUrls(html) + relUrlsAsAbs(url = url, html = html)).distinct()
        val filterPatterns: ((Regex) -> Boolean) -> Boolean = { predicate ->
            when (filter) {
                is Blacklist -> filter.disallowPatterns.none(predicate)
                is Whitelist -> filter.allowPatterns.any(predicate)
            }
        }
        return extractedUrls.filter { absUrl -> filterPatterns { pattern -> pattern.matches(absUrl) } }
    }
}