package dev.yekta.krawler.tui.model

import dev.yekta.krawler.console.Ask

data class Menu(
    val title: String,
    val options: List<Ask.Option>
)
