package com.crawler.database.repository

import cats.effect.kernel.Sync
import cats.syntax.flatMap.given
import cats.syntax.functor.given
import com.crawler.domain.title.{GotStatus, TitleResult}
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.Instant

trait TitleRepository[F[_]]:
  def select(url: String): F[Option[TitleResult]]
  def insert(result: TitleResult, ttl: Instant): F[Unit]
  def deleteOlderThan(time: Instant): F[Unit]

private final class TitleRepositoryImpl[F[_]: Sync](
    log: Logger[F],
    appVersion: String,
    transactor: HikariTransactor[F]
) extends TitleRepository[F]:
  override def select(url: String): F[Option[TitleResult]] =
    sql"""
      SELECT url, data, status 
      FROM titles 
      WHERE url = $url AND version = $appVersion
      ORDER BY ttl DESC
      LIMIT 1
    """
      .query[TitleResult]
      .option
      .transact(transactor)

  override def insert(
      result: TitleResult,
      ttl: Instant
  ): F[Unit] =
    def insert(url: String, data: String, status: GotStatus) =
      log.debug(s"Insert data for task $url") >>
        sql"""
         INSERT INTO titles (url, data, status, ttl, version)
         VALUES ($url, $data, $status, $ttl, $appVersion)
         ON CONFLICT (
           url
         ) DO UPDATE SET
           ttl = $ttl,
           version = $appVersion,
           status = $status,
           data = $data
      """.update.run
          .transact(transactor)
          .void

    result match
      case TitleResult.Success(url, title, status) => insert(url, title, status)
      case TitleResult.Failure(url, error, status) => insert(url, error, status)

  override def deleteOlderThan(time: Instant): F[Unit] =
    log.debug(s"Delete cached urls older than $time") >>
      sql"""
         DELETE FROM titles
         WHERE ttl < $time
      """.update.run
        .transact(transactor)
        .void

object TitleRepositoryImpl:
  def make[F[_]: Sync](
      transactor: HikariTransactor[F],
      appVersion: String
  ): F[TitleRepository[F]] =
    Slf4jLogger
      .fromName("UrlRepository")
      .map(TitleRepositoryImpl(_, appVersion, transactor))
