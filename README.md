# Krawler: Asynchronous Kotlin Crawler

## Overview

Krawler is a fully configurable and asynchronous HTML Crawler written in Kotlin (JVM). Powered by **Coroutines**,
**Kotlin Serialization (JSON)**, **Ktor Client**, **Exposed**, **SQLite**, and **SQLite JDBC**, Krawler provides a way
to easily scrape HTML webpages.

## Features

- **Asynchronous Processing**: Utilizing Kotlin's coroutines, Krawler is designed for high-performance, concurrent web
  crawling.

- **Configurability**: Krawler is highly customizable through the `krawler_config.json` file, placed at the project
  path.

- **Extensive Logging**: Verbose logs can be enabled via the configuration file.

- **Persisting Errors**: Errors during the crawling process are stored in the `CrawlErrors` table (with the necessary
  metadata) and printed to the standard output.

## Database Schema

Krawler uses the following tables to persist data:

```
CrawlActivities : IntIdTable() {
  varchar("sessionId", 100)
  long("atEpochSeconds")
  varchar("type", 50)
}

CrawlErrors : IntIdTable() {
  varchar("sessionId", 100)
  long("atEpochSeconds")
  text("url")
  text("error")
}

CrawlingStates : IntIdTable() {
  varchar("sessionId", 100)
  text("url")
  integer("depth")
  long("priority")
}

Webpages : IntIdTable() {
  varchar("sessionId", 100)
  long("atEpochSeconds")
  text("url")
  text("html")
}
```

## Configuration

Krawler is highly customizable through the `krawler_config.json` file, placed at the project path. Below is a sample
configuration containing all settings:

  ```json
  {
  "seeds": [
    "https://en.wikipedia.org/wiki/NASA"
  ],
  "filter": {
    "#": "dev.yekta.krawler.model.CrawlingFilter.Whitelist",
    "allowPatterns": [
      "https://en\\.wikipedia\\.org/wiki/.*"
    ]
  },
  "depth": 8,
  "maxPages": 100,
  "maxPageSizeKb": null,
  "concurrentConnections": 16,
  "verbose": true,
  "shouldFollowRedirects": true,
  "userAgent": "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
  "connectTimeoutMs": 6000,
  "readTimeoutMs": 6000,
  "retriesOnServerError": 0,
  "customHeaders": null
}
  ```

+ **`seeds`**: Starting URLs for crawling.
+ **`filter`**: Crawling filter configuration, either **Whitelist** or **Blacklist**.
+ **`depth`**: Maximum depth of crawling.
+ **`maxPages`**: Maximum number of pages to crawl.
+ **`maxPageSizeKb`**: Maximum page size in kilobytes.
+ **`concurrentConnections`**: Number of concurrent connections for crawling.
+ **`verbose`**: Enable verbose logging.
+ **`shouldFollowRedirects`**: Specify if redirects should be followed.
+ **`userAgent`**: User agent string for HTTP requests.
+ **`connectTimeoutMs`**: Connection timeout in milliseconds.
+ **`readTimeoutMs`**: Read timeout in milliseconds.
+ **`retriesOnServerError`**: Number of retries on server errors (`5xx`).
+ **`customHeaders`**: Additional custom headers for HTTP requests.

## Good Next Steps

Things that would benefit Krawler the most:

+ Implementing Pause/Resume
    + _Hint:_ The `UrlPool` is the only state that isn't currently being persisted but needs to be, in order to be able
      to restore paused sessions.
+ Config: `respectRobotsTxt: Boolean`
+ Config: `consecutiveErrorsToPause: Int?`

## Disclaimer

Krawler was conceived and brought to life over a weekend, starting as a pet project. It was initially planned to be made
as a component of the coursework for the Web & Search Engines course at Yazd University, then growing exponentially due
to a sudden desire to make a "good thing" out of it! It's important to note that, no explicit guarantees are extended
regarding its correctness of functionality, support, or any other aspect. With that in mind, happy Krawling!

## License

Please **refer to [LICENSE](./LICENSE)** to view the project's license.
