package com.crawler.service.server

import cats.effect.kernel.{Async, Resource}
import cats.syntax.functor.given
import com.crawler.config.server.ServerConfig
import com.crawler.endpoint.{LivenessProbeEndpoint, SwaggerEndpoint}
import com.crawler.service.server.cors.CorsHttp4s
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.{HttpApp, HttpRoutes}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

trait HttpServer[F[_], S]:
  def start: Resource[F, S]

private final class HttpServerImpl[F[_]: Async](
    log: Logger[F],
    config: ServerConfig,
    endpoints: List[ServerEndpoint[Fs2Streams[F], F]]
) extends HttpServer[F, Server]:

  override def start: Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withLogger(log)
      .withHost(config.host)
      .withPort(config.port)
      .withHttpApp(httpApp)
      .build

  private def httpApp: HttpApp[F] =
    val livenessProbeRoute = LivenessProbeEndpoint.make[F]
    val applicationRoutes  = livenessProbeRoute +: endpoints
    val swaggerRoute       = SwaggerEndpoint.make[F](config.swagger, applicationRoutes)

    val httpRoutes =
      CorsHttp4s.default(
        Http4sServerInterpreter[F]()
          .toRoutes(applicationRoutes ++ swaggerRoute)
      )

    Router("/" -> httpRoutes).orNotFound

object HttpServerImpl:
  def make[F[_]: Async](
      config: ServerConfig,
      endpoints: List[ServerEndpoint[Fs2Streams[F], F]]
  ): F[HttpServer[F, Server]] =
    Slf4jLogger
      .fromName("HttpServer")
      .map(HttpServerImpl[F](_, config, endpoints))
