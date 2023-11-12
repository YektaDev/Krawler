package dev.yekta.krawler.tui.model

interface Stack<T> {
    val active: T
    val hasBack: Boolean

    fun push(value: T)
    fun pop()
}
