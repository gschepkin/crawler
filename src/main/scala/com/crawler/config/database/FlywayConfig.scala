package com.crawler.config.database

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

final case class FlywayConfig(
    validateMigrationNaming: Boolean,
    cleanDisabled: Boolean,
    locations: List[String]
) derives ConfigReader
