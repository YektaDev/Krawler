package dev.yekta.krawler.tui

sealed interface KrawlerStartResult {
    data object Success : KrawlerStartResult
    data class Failure(val reason: String) : KrawlerStartResult
}