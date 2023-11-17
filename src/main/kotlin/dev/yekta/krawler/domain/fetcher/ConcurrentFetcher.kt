package dev.yekta.krawler.domain.fetcher

interface ConcurrentFetcher {
    val maxConnections: Int

    suspend fun fetch(url: String, onRead: suspend (FetchResult) -> Unit)
}