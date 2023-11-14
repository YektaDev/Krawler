package dev.yekta.krawler.repo

data class RepoImp(
    override val crawlingState: CrawlingStateStore,
    override val webpage: WebpageStore,
    override val activity: CrawlActivityStore,
    override val error: CrawlErrorStore,
) : Repo
