package com.crawler.config.database

import com.zaxxer.hikari.HikariConfig
import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

import scala.concurrent.duration.FiniteDuration

final case class PostgresConfig(
    postgresHost: String,
    postgresPort: Int,
    postgresDb: String,
    username: String,
    password: String,
    maximumPoolSize: Int,
    executionContextPoolSize: Int,
    driverClassName: String,
    autoCommit: Boolean,
    connectionTimeout: FiniteDuration,
) derives ConfigReader:
  def jdbcUrl = s"jdbc:postgresql://$postgresHost:$postgresPort/$postgresDb"

  def toHikariConfig: HikariConfig =
    val config = new HikariConfig()

    config.setDriverClassName(driverClassName)
    config.setJdbcUrl(jdbcUrl)
    config.setPassword(password)
    config.setUsername(username)
    config.setAutoCommit(autoCommit)
    config.setConnectionTimeout(connectionTimeout.toMillis)
    config.setMaximumPoolSize(maximumPoolSize)

    config
