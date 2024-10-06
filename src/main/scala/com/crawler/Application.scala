package com.crawler

import cats.Parallel
import cats.effect.kernel.Async
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.flatMap.given
import cats.syntax.functor.given
import com.crawler.config.AppConfig
import com.crawler.database.migration.FlywayImpl
import com.crawler.database.repository.{TitleRepository, TitleRepositoryImpl}
import com.crawler.database.transactor.DoobieTransactor
import com.crawler.endpoint.CrawlEndpoint
import com.crawler.service.cache.TitleCacheServiceImpl
import com.crawler.service.client.HttpClient
import com.crawler.service.crawl.CrawlServiceImpl
import com.crawler.service.parse.HtmlTitleParser
import com.crawler.service.server.{HttpServer, HttpServerImpl}
import com.crawler.service.title.TitleServiceImpl
import org.http4s.server.Server
import sttp.capabilities.fs2.Fs2Streams
import sttp.client4.StreamBackend
import sttp.client4.httpclient.fs2.HttpClientFs2Backend

object Application extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    val init =
      for
        config          <- AppConfig.load[IO].toResource
        backend         <- HttpClientFs2Backend.resource[IO]()
        transactor      <- DoobieTransactor.resource[IO](config.postgres)
        flyway          <- FlywayImpl.make[IO](config.flyway, config.postgres).toResource
        _               <- flyway.migrate.toResource
        titleRepository <- TitleRepositoryImpl.make[IO](transactor, config.version).toResource
        _               <- TitleCacheServiceImpl.cleaner[IO](config.cache.cleaner, titleRepository)
      yield (config, backend, titleRepository)

    init.evalMap { case (config, backend, titleRepository) => program[IO](config, backend, titleRepository) }
      .flatMap(_.start)
      .useForever
      .void
      .as(ExitCode.Success)

  private def program[F[_]: Async: Parallel](
      config: AppConfig,
      backend: StreamBackend[F, Fs2Streams[F]],
      titleRepository: TitleRepository[F]
  ): F[HttpServer[F, Server]] =
    for
      client            <- HttpClient.make[F](config.client, backend)
      parser            <- HtmlTitleParser.make[F]()
      titleService      <- TitleServiceImpl.make(client, parser)
      cacheTitleService <- TitleCacheServiceImpl.make[F](config.cache, titleService, titleRepository)
      crawlService      <- CrawlServiceImpl.make(config.crawl, cacheTitleService)
      crawlEndpoint      = CrawlEndpoint.make[F](config.security, crawlService)
      server            <- HttpServerImpl.make[F](config.server, List(crawlEndpoint))
    yield server
