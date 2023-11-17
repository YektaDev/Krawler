package dev.yekta.krawler.domain.fetcher

import dev.yekta.krawler.log.Log
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit.MILLISECONDS

class ConcurrentFetcherImp(
    override val maxConnections: Int = 4,
    private val shouldFollowRedirects: Boolean = true,
    private val userAgent: String = "Krawler",
    private val connectTimeoutMs: Long = 10_000,
    private val readTimeoutMs: Long = 10_000,
    private val retriesOnServerError: Int = 0,
    private val customHeaders: List<Pair<String, String>>? = null,
) : ConcurrentFetcher {
    private var activeConnections = MutableStateFlow(0)
    private val client = HttpClient(OkHttp) {
        installCustomHeaders()
        installRetries()
        install(UserAgent) { agent = userAgent }
        engine {
            config {
                followRedirects(shouldFollowRedirects)
                connectTimeout(connectTimeoutMs, MILLISECONDS)
                readTimeout(readTimeoutMs, MILLISECONDS)
            }
        }
    }

    private fun HttpClientConfig<OkHttpConfig>.installCustomHeaders() {
        if (customHeaders.isNullOrEmpty()) return
        install(DefaultRequest) {
            customHeaders.forEach { (k, v) ->
                header(k, v)
            }
        }
    }

    private fun HttpClientConfig<OkHttpConfig>.installRetries() {
        if (retriesOnServerError <= 0) return
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = retriesOnServerError)
            exponentialDelay()
        }
    }

    override suspend fun fetch(url: String, onRead: suspend (FetchResult) -> Unit) = launchOrSuspendRead(url, onRead)

    private suspend inline fun launchOrSuspendRead(url: String, crossinline onRead: suspend (FetchResult) -> Unit) {
        if (activeConnections.value >= maxConnections) return onRead(read(url))
        activeConnections.value++
        client.launch {
            val readResult = read(url)
            activeConnections.value--
            onRead(readResult)
        }
    }

    private suspend fun read(url: String): FetchResult = try {
        Log.v("GET: $url")
        val response = client.get(urlString = url)
        when {
            response.status.isSuccess().not() -> readError(url, response.status.description)
            response.contentType() != ContentType.Text.Html -> FetchResult.NotHtml(response.contentType()?.contentType)
            else -> FetchResult.Html(response.bodyAsText())
        }
    } catch (e: Throwable) {
        readError(url, error = e.message ?: e.stackTraceToString())
    }

    private fun readError(url: String, error: String): FetchResult {
        Log.e("[$url]: $error")
        return FetchResult.ReadError(error)
    }
}