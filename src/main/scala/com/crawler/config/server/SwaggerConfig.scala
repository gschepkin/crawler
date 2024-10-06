package com.crawler.config.server

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

final case class SwaggerConfig(
    enabled: Boolean,
    endpoint: String,
    title: String,
    apiVersion: String
) derives ConfigReader:
  def getEndpointPath: List[String] =
    endpoint.split("/").toList
