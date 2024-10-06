package com.crawler.config

import cats.effect.kernel.Sync
import com.crawler.config.cache.CacheConfig
import com.crawler.config.client.ClientConfig
import com.crawler.config.crawl.CrawlConfig
import com.crawler.config.database.{ FlywayConfig, PostgresConfig }
import com.crawler.config.security.SecurityConfig
import com.crawler.config.server.ServerConfig
import pureconfig.{ ConfigReader, ConfigSource }
import pureconfig.generic.derivation.default.*

final case class AppConfig(
    version: String,
    crawl: CrawlConfig,
    flyway: FlywayConfig,
    postgres: PostgresConfig,
    server: ServerConfig,
    client: ClientConfig,
    cache: CacheConfig,
    security: SecurityConfig
) derives ConfigReader

object AppConfig:
  def load[F[_]: Sync]: F[AppConfig] =
    Sync[F].delay {
      ConfigSource.defaultApplication.loadOrThrow[AppConfig]
    }
