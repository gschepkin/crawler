package com.crawler.endpoint

import cats.Applicative
import cats.syntax.either.given
import cats.syntax.applicative.given
import com.crawler.domain.server.HttpResponse
import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full

object LivenessProbeEndpoint:
  def make[F[_]: Applicative]: Full[Unit, Unit, Unit, HttpResponse[String, String], HttpResponse[String, String], Any, F] =
    endpoint
      .in("health")
      .out(jsonBody[HttpResponse[String, String]])
      .errorOut(jsonBody[HttpResponse[String, String]])
      .description("Check liveness status")
      .serverLogic { _ =>
        HttpResponse
          .success[String, String]("I am healthy!")
          .asRight[HttpResponse[String, String]]
          .pure[F]
      }
