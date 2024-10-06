package com.crawler.service.client

import cats.syntax.flatMap.given
import cats.syntax.functor.given
import cats.syntax.either.given
import cats.effect.kernel.{Async, Sync}
import com.crawler.config.client.ClientConfig
import com.crawler.service.client.HttpClient.UrlLoaderError
import com.crawler.service.crawl.{CrawlService, CrawlServiceImpl}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.capabilities.fs2.Fs2Streams
import sttp.client4.StreamBackend
import sttp.model.Uri
import sttp.client4.*

trait Client[F[_]]:
  def get(uri: Uri): F[String]

private final class HttpClient[F[_]: Sync](
    log: Logger[F],
    config: ClientConfig,
    backend: StreamBackend[F, Fs2Streams[F]],
) extends Client[F]:
  override def get(uri: Uri): F[String] =
    for
      _        <- log.info(s"Loading by URI: $uri")
      response <- basicRequest
                    .get(uri)
                    .readTimeout(config.timeoutPerRequest)
                    .send(backend)
      _        <- log.debug(s"Got response for $uri: $response")
      body      = response.body.leftMap(UrlLoaderError.apply)
      result   <- Sync[F].fromEither(body)
    yield result

object HttpClient:
  final case class UrlLoaderError(message: String) extends Throwable(message)

  def make[F[_]: Async](
      config: ClientConfig,
      backend: StreamBackend[F, Fs2Streams[F]]
  ): F[Client[F]] =
    Slf4jLogger
      .fromName("HttpClient")
      .map(HttpClient(_, config, backend))
