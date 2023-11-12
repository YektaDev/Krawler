package dev.yekta.krawler.tui

import dev.yekta.krawler.model.Page
import dev.yekta.krawler.model.Page.MAIN_MENU

class TuiStateManager(
    private var page: Page = MAIN_MENU,
    private val pages: (Page) -> Unit,
) {
    fun bindScreen() {
        while (true) {
            pages(page)
        }
    }

    fun navigate(page: Page) {
        this.page = page
    }
}
