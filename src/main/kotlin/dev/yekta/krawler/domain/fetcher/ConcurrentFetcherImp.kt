package dev.yekta.krawler.domain.fetcher

import dev.yekta.krawler.log.Log
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
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
    private val customHeaders: Map<String, String>? = null,
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

    override suspend fun fetch(url: String, onRead: suspend (FetchResult) -> Unit) =
        launchOrSuspendRead(url, onRead)

    private suspend inline fun launchOrSuspendRead(url: String, crossinline onRead: suspend (FetchResult) -> Unit) =
        when {
            activeConnections.value >= maxConnections -> launchRequest { onRead(read(url)) }
            else -> {
                activeConnections.value++
                launchRequest {
                    val readResult = read(url)
                    activeConnections.value--
                    onRead(readResult)
                }
            }
        }

    private suspend inline fun launchRequest(crossinline block: suspend CoroutineScope.() -> Unit) {
        client.launch(SupervisorJob()) {
            block()
        }
    }

    private suspend fun read(url: String): FetchResult = try {
        Log.v("[$url] Get")
        val response = client.get(urlString = url)
        Log.v("[$url] Get Response Code: ${response.status}")

        response.toFetchResult(url)
    } catch (e: Throwable) {
        readError(url, error = e.message ?: e.stackTraceToString())
    }

    private suspend fun HttpResponse.toFetchResult(url: String): FetchResult {
        val type = contentType()?.contentType
        return when {
            !status.isSuccess() -> readError(url, status.description)
            type != "text" -> FetchResult.NotHtml(type)
            else -> {
                val body = bodyAsText()
                when (isContentHtml(body)) {
                    true -> FetchResult.Html(body)
                    false -> FetchResult.NotHtml(type)
                }
            }
        }
    }

    private fun isContentHtml(content: String): Boolean {
        val trimmed = content.trimStart()
        return when {
            trimmed.startsWith("<!doctype html>", ignoreCase = true) -> true
            trimmed.contains("<html>", ignoreCase = true) -> true
            else -> false
        }
    }

    private fun readError(url: String, error: String): FetchResult {
        Log.e("[$url]: $error")
        return FetchResult.ReadError(error)
    }
}