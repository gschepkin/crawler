package com.crawler.config.cache

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

import scala.concurrent.duration.FiniteDuration

final case class CacheConfig(
    enabled: Boolean,
    titleTtl: TitleTtlConfig,
    cleaner: CleanerConfig
) derives ConfigReader
