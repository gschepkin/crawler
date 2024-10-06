package com.crawler.endpoint

import cats.effect.kernel.Sync
import cats.syntax.applicative.given
import cats.syntax.applicativeError.given
import cats.syntax.either.given
import cats.syntax.functor.given
import com.crawler.config.security.SecurityConfig
import com.crawler.domain.cache.CacheResult
import com.crawler.domain.server.HttpResponse
import com.crawler.domain.title.TitleResult
import com.crawler.service.crawl.CrawlService
import sttp.model.QueryParams
import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full

object CrawlEndpoint:
  private type Result = List[CacheResult[TitleResult]]

  def make[F[_]: Sync](
      config: SecurityConfig,
      crawlService: CrawlService[F, String, CacheResult[TitleResult]]
  ): Full[String, Unit, List[String], HttpResponse[String, Result], HttpResponse[String, Result], Any, F] =
    def returnError[L](message: String) =
      HttpResponse.failure[String, Result](message).asLeft[L]

    SecurityEndpoint
      .make[F, Result](config)
      .in("crawl")
      .in(query[List[String]]("url"))
      .serverLogic { _ => urls =>
        val finalUrls = urls.filter(_.nonEmpty)
        crawlService.crawl(finalUrls).attempt.map {
          case Right(result) => HttpResponse.success[String, Result](result).asRight
          case Left(error)   => returnError(error.getMessage)
        }
      }
