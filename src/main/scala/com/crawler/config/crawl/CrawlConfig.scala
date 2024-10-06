package com.crawler.config.crawl

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

final case class CrawlConfig(
    maxPoolRequests: Int
) derives ConfigReader
