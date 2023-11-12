package dev.yekta.krawler.domain

import dev.yekta.krawler.model.KrawlerSettings

interface Crawler {
    fun start()
    fun stop()
    fun pause()
    fun resume()
}

class CrawlerImp(
    private val settings: KrawlerSettings,
) : Crawler {
    override fun start() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun resume() {
        TODO("Not yet implemented")
    }
}