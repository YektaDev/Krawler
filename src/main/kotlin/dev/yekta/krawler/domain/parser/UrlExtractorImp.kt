package dev.yekta.krawler.domain.parser

import dev.yekta.krawler.model.CrawlingFilter
import dev.yekta.krawler.model.CrawlingFilter.Blacklist
import dev.yekta.krawler.model.CrawlingFilter.Whitelist

class UrlExtractorImp(override val filter: CrawlingFilter): UrlExtractor {
    private companion object {
        @Suppress("HttpUrlsUsage")
        val validPrefixes = arrayOf(
            "http://",
            "https://",
            "ftp://",
            "www.",
        )

        val validChars = (
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                        "abcdefghijklmnopqrstuvwxyz" +
                        "0123456789" +
                        "-._~:/?#[]@!\$&'()*+,;=%"
                ).toCharArray().sortedArray()
    }

    private fun validUrlsOf(html: String): List<String> {
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

    override fun extract(html: String): List<String> {
        val possibleUrls = validUrlsOf(html)
        val filterPatterns: ((Regex) -> Boolean) -> Boolean = { predicate ->
            when (filter) {
                is Blacklist -> filter.disallowPatterns.none(predicate)
                is Whitelist -> filter.allowPatterns.any(predicate)
            }
        }
        val filteredUrls = possibleUrls.filter { url ->
            filterPatterns { pattern -> pattern.matches(url) }
        }
        return filteredUrls
    }
}