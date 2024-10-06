package com.crawler.config.client

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

import scala.concurrent.duration.FiniteDuration

final case class ClientConfig(
    timeoutPerRequest: FiniteDuration
) derives ConfigReader
