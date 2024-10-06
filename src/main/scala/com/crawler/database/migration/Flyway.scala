package com.crawler.database.migration

import cats.Applicative
import cats.effect.Sync
import cats.syntax.applicative.given
import cats.syntax.flatMap.given
import cats.syntax.functor.given
import com.crawler.config.database.{FlywayConfig, PostgresConfig}
import org.flywaydb.core.Flyway as JFlyway
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait Flyway[F[_]] {
  def clean: F[Unit]
  def migrate: F[Unit]
}

private final class FlywayImpl[F[_]: Sync](log: Logger[F], jFlyway: JFlyway) extends Flyway[F]:
  def clean: F[Unit] =
    for
      _ <- log.info("Start database cleanup")
      _ <- Sync[F].delay(jFlyway.clean())
      _ <- log.info("Database cleanup finished")
    yield ()

  def migrate: F[Unit] =
    for
      _ <- log.info("Start database migration")
      _ <- Sync[F].delay(jFlyway.migrate()).void
      _ <- log.info("Database migration finished")
    yield ()

object FlywayImpl:
  def make[F[_]: Sync](flywayConfig: FlywayConfig, postgresConfig: PostgresConfig): F[Flyway[F]] =
    def flyway =
      JFlyway
        .configure()
        .dataSource(postgresConfig.jdbcUrl, postgresConfig.username, postgresConfig.password)
        .validateMigrationNaming(flywayConfig.validateMigrationNaming)
        .cleanDisabled(flywayConfig.cleanDisabled)
        .locations(flywayConfig.locations*)
        .load()

    for
      log    <- Slf4jLogger.fromName[F]("Flyway")
      flyway <- Sync[F].delay(FlywayImpl[F](log, flyway))
    yield flyway
