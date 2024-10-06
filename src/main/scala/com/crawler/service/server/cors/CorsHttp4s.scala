package com.crawler.service.server.cors

import cats.data.OptionT
import cats.effect.Async
import org.http4s.server.middleware.CORS
import org.http4s.{Http, HttpRoutes}

object CorsHttp4s:
  def default[F[_]: Async](routes: HttpRoutes[F]): Http[[_$7] =>> OptionT[F, _$7], F] =
    CORS.policy.withAllowOriginAll
      .withAllowCredentials(true)
      .withAllowOriginHost(_.host.value == "localhost")
      .apply(routes)
