package com.crawler.config.cache

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

import scala.concurrent.duration.FiniteDuration

final case class TitleTtlConfig(
    success: FiniteDuration,
    failure: FiniteDuration
) derives ConfigReader
