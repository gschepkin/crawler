package com.crawler.endpoint

import cats.effect.kernel.Sync
import com.crawler.config.server.SwaggerConfig
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object SwaggerEndpoint:
  def make[F[_]: Sync](
      config: SwaggerConfig,
      endpoints: List[ServerEndpoint[Fs2Streams[F], F]]
  ): List[ServerEndpoint[Any, F]] =
    def swaggerRoutes =
      SwaggerInterpreter(swaggerUIOptions = SwaggerUIOptions.default.pathPrefix(config.getEndpointPath))
        .fromServerEndpoints[F](
          endpoints = endpoints,
          title = config.title,
          version = config.apiVersion
        )

    if config.enabled
    then swaggerRoutes
    else List.empty
