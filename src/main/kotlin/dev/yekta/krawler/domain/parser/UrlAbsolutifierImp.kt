package dev.yekta.krawler.domain.parser

import dev.yekta.krawler.domain.parser.ValidUrlData.validPrefixes
import kotlin.math.min

class UrlAbsolutifierImp : UrlAbsolutifier {
    private companion object {
        val hasAbsolutePrefix: (String) -> Boolean = { str -> validPrefixes.any { prefix -> str.startsWith(prefix) } }

        fun Char.isQuote() = this == '"' || this == '\''

        val relPrefixes = arrayOf(
            " href=",
            " codebase=",
            " cite=",
            " background=",
            " action=",
            " longdesc=",
            " src=",
            " profile=",
            " usemap=",
            " classid=",
            " data=",
            " formaction=",
            " icon=",
            " manifest=",
            " poster=",
        )

        fun getUrlDomain(url: String): String {
            val startIndex = validPrefixes.firstOrNull { prefix -> url.startsWith(prefix) }?.length ?: 0
            val separatorIndex = url.indexOf(string = "/", startIndex = startIndex)
                .takeIf { it >= 0 } ?: return url

            return url.substring(startIndex = 0, endIndex = separatorIndex)
        }

        fun getUrlWithoutParams(url: String) = url.substringBefore('?').trimEnd('/')

        fun possibleRelativeUrls(html: String) = unquotedTokensPossiblyStartingWithUrl(html) +
                singleQuotedTokensPossiblyStartingWithUrl(html) +
                doubleQuotedTokensPossiblyStartingWithUrl(html)

        private fun unquotedTokensPossiblyStartingWithUrl(html: String): List<String> {
            val regex = relPrefixes
                .joinToString(prefix = "", postfix = "", separator = "|")
                .let(::Regex)
            return tokensOfPrefixedRelUrls(html, urlPrefixRegex = regex)
                .filterNot { it[0].isQuote() }
                .map { token ->
                    val i1 = token.indexOf(' ').takeIf { it >= 0 } ?: token.length
                    val i2 = token.indexOf('>').takeIf { it >= 0 } ?: token.length
                    val delimiterIndex = min(i1, i2)
                    token.substring(0, delimiterIndex)
                }
                .toList()
        }

        private fun singleQuotedTokensPossiblyStartingWithUrl(html: String) =
            tokensPossiblyStartingWithUrl(html, wrappedIn = '\'')

        private fun doubleQuotedTokensPossiblyStartingWithUrl(html: String) =
            tokensPossiblyStartingWithUrl(html, wrappedIn = '"')

        private fun tokensPossiblyStartingWithUrl(html: String, wrappedIn: Char): List<String> {
            val regex = relPrefixes
                .joinToString(prefix = "", postfix = "", separator = "|") { "$it$wrappedIn" }
                .let(::Regex)
            return tokensOfPrefixedRelUrls(html, urlPrefixRegex = regex)
                .map { it.substringBefore(wrappedIn.toString()) }
                .toList()
        }

        private fun tokensOfPrefixedRelUrls(html: String, urlPrefixRegex: Regex) = html
            .split(urlPrefixRegex)
            .drop(1)
            .asSequence()
            .map(String::trimStart)
            .filterNot(hasAbsolutePrefix)
    }

    override fun extractRelUrlsAsAbsolute(url: String, html: String): List<String> {
        val domain = getUrlDomain(url)
        val path = url.substringBeforeLast('/')
        return possibleRelativeUrls(html)
            .asSequence()
            .filter { it[0] == '/' || it[0].isLetterOrDigit() }
            .map { rel ->
                when {
                    rel.startsWith('/') -> domain + rel
                    else -> "$path/$rel"
                }
            }
            .toList()
    }

}