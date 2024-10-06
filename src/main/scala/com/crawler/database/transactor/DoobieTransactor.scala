package com.crawler.database.transactor

import cats.effect.{Async, Resource}
import com.crawler.config.database.PostgresConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object DoobieTransactor:
  def resource[F[_]: Async](config: PostgresConfig): Resource[F, HikariTransactor[F]] =
    ExecutionContexts.fixedThreadPool[F](config.executionContextPoolSize).flatMap { executionContext =>
      HikariTransactor.fromHikariConfigCustomEc[F](
        hikariConfig = config.toHikariConfig,
        connectEC = executionContext
      )
    }
