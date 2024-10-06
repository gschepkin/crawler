package com.crawler.config.server

import com.comcast.ip4s.{Host, Port}
import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

final case class ServerConfig(
    host: Host,
    port: Port,
    swagger: SwaggerConfig
)

object ServerConfig:
  given ConfigReader[Host]         = ConfigReader.fromStringOpt(Host.fromString)
  given ConfigReader[Port]         = ConfigReader.fromStringOpt(Port.fromString)
  given ConfigReader[ServerConfig] = ConfigReader.derived
