package dev.yekta.krawler.domain.fetcher

sealed interface FetchResult {
    @JvmInline
    value class Html(val html: String) : FetchResult

    @JvmInline
    value class ReadError(val message: String) : FetchResult

    @JvmInline
    value class NotHtml(val contentType: String?) : FetchResult
}
