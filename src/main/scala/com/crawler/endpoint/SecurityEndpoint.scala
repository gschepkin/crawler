package com.crawler.endpoint

import cats.effect.kernel.Sync
import cats.syntax.applicative.given
import cats.syntax.either.given
import com.crawler.config.security.SecurityConfig
import com.crawler.domain.cache.CacheResult
import com.crawler.domain.server.HttpResponse
import com.crawler.domain.title.TitleResult
import com.crawler.endpoint.CrawlEndpoint.Result
import io.circe.{Decoder, Encoder}
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.*
import sttp.tapir.server.PartialServerEndpoint

object SecurityEndpoint:
  private case object AccessDenied extends Throwable("Access Denied, you need to use correct access token")

  def make[
      F[_]: Sync,
      Result: Encoder: Decoder: Schema
  ](config: SecurityConfig): PartialServerEndpoint[String, Unit, Unit, HttpResponse[String, Result], HttpResponse[String, Result], Any, F] =
    endpoint
      .securityIn(query[String]("access-token"))
      .out(jsonBody[HttpResponse[String, Result]])
      .errorOut(jsonBody[HttpResponse[String, Result]])
      .description("Use to crawl for all urls from params")
      .serverSecurityLogic[Unit, F] {
        case token if token == config.accessToken =>
          ().asRight.pure[F]

        case _ =>
          HttpResponse.failure[String, Result](AccessDenied.getMessage).asLeft.pure[F]
      }
