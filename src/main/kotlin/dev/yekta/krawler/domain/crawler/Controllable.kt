package dev.yekta.krawler.domain.crawler

interface Controllable {
    fun start()
    fun stop()
    fun pause()
    fun resume()
}
