package dev.yekta.krawler.tui

import dev.yekta.krawler.console.Ask
import dev.yekta.krawler.console.error
import dev.yekta.krawler.model.Page
import dev.yekta.krawler.tui.model.Menu
import dev.yekta.krawler.tui.model.Stack

interface TuiStackNavigator {
    val active: Page
    val hasBack: Boolean

    fun bindTui()

    fun pop()
    fun push(page: Page)
    fun pushOrReplace(page: Page)
}

class TuiStackNavigatorImp(page: Page = Page.MAIN_MENU, pages: (Page) -> Menu) : TuiStackNavigator {
    private val stack = object : Stack<Page> {
        private val _stack = arrayListOf(page)

        override val active get() = _stack.last()
        override val hasBack get() = _stack.size > 1

        override fun pop() {
            _stack.removeLast()
        }

        override fun push(value: Page) {
            _stack.add(page)
        }
    }

    override val active: Page get() = stack.active
    override val hasBack: Boolean get() = stack.hasBack

    override fun pop() {
        if (!hasBack) return error("Cannot go back more than this!")
        stack.pop()
        manager.navigate(active)
    }

    override fun push(page: Page) {
        stack.push(page)
        manager.navigate(page)
    }

    override fun pushOrReplace(page: Page) {
        if (active == page) stack.pop()
        push(page)
    }

    override fun bindTui() = manager.bindScreen()

    private val manager = TuiStateManager(page) { page ->
        val menu = pages(page)
        handleMenu(
            title = menu.title,
            options = menu.options.toTypedArray(),
            back = { pop() }.takeIf { stack.hasBack },
        )
    }

    private fun handleMenu(title: String, back: (() -> Unit)? = null, vararg options: Ask.Option) {
        fun ask(options: List<Ask.Option>) = Ask.optionsInSection(
            sectionTitle = title,
            options = options.toTypedArray(),
        )

        val optionList = options.toList()
        back ?: return ask(optionList).action()

        val backCode = (optionList.size + 1).toString()
        val optionListWithBack = optionList + Ask.Option(backCode, "Back", back)

        val selectedOption = ask(optionListWithBack)
        if (selectedOption.code == backCode) back() else selectedOption.action()
    }
}