package dev.yekta.krawler.repo

interface Repo {
    val crawlingState: CrawlingStateStore
    val webpage: WebpageStore
    val activity: CrawlActivityStore
    val error: CrawlErrorStore
}
