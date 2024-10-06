package com.crawler.domain.server

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sttp.tapir.Schema

final case class HttpResponse[L, R](
    data: Option[R],
    error: Option[L]
)

object HttpResponse:
  def success[L: Encoder, R: Encoder](result: R): HttpResponse[L, R] =
    HttpResponse[L, R](
      data = Some(result),
      error = Option.empty[L]
    )

  def failure[L: Encoder, R: Encoder](error: L): HttpResponse[L, R] =
    HttpResponse[L, R](
      data = Option.empty[R],
      error = Some(error)
    )

  given [P: Encoder, E: Encoder]: Encoder[HttpResponse[P, E]] = deriveEncoder
  given [P: Decoder, E: Decoder]: Decoder[HttpResponse[P, E]] = deriveDecoder
  given [P: Schema, E: Schema]: Schema[HttpResponse[P, E]]    = Schema.derived
