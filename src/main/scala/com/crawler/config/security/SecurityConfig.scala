package com.crawler.config.security

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

final case class SecurityConfig (
    accessToken: String
) derives ConfigReader
