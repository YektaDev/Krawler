package dev.yekta.krawler.domain.parser

object ValidUrlData {
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