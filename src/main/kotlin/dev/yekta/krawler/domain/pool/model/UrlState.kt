package dev.yekta.krawler.domain.pool.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface UrlState {
    val minDepth: Int
    fun copy(minDepth: Int): UrlState

    @Serializable
    class NotVisited(override val minDepth: Int) : UrlState {
        override fun copy(minDepth: Int) = NotVisited(minDepth)
    }

    @Serializable
    sealed class Visited : UrlState {
        class Html(override val minDepth: Int) : UrlState {
            override fun copy(minDepth: Int) = Html(minDepth)
        }

        class NonHtml(override val minDepth: Int) : UrlState {
            override fun copy(minDepth: Int) = NonHtml(minDepth)
        }
    }

    @Serializable
    class ReadError(override val minDepth: Int) : UrlState {
        override fun copy(minDepth: Int) = ReadError(minDepth)
    }
}
