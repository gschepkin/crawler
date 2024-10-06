package com.crawler.domain.title

import cats.syntax.either.given
import doobie.{Read, Write}
import io.circe.{Decoder, Encoder}
import sttp.tapir.Schema

enum GotStatus(val value: String):
  case Success extends GotStatus("success")
  case Failure extends GotStatus("failure")

object GotStatus:
  given Encoder[GotStatus] = Encoder.encodeString.contramap(_.value)
  given Decoder[GotStatus] = Decoder.decodeString.emap(fromString(_).leftMap(_.getMessage))
  given Schema[GotStatus]  = Schema.derived
  given Read[GotStatus]    = Read[String].map(fromString).map(_.getOrElse(GotStatus.Failure))
  given Write[GotStatus]   = Write[String].contramap(_.value)

  private def fromString(value: String): Either[NotSupportedGotStatus, GotStatus] =
    value.trim.toLowerCase match
      case GotStatus.Success.value => GotStatus.Success.asRight[NotSupportedGotStatus]
      case GotStatus.Failure.value => GotStatus.Failure.asRight[NotSupportedGotStatus]
      case value                   => NotSupportedGotStatus(value).asLeft[GotStatus]

  final case class NotSupportedGotStatus(value: String) extends IllegalArgumentException(s"GotStatus '$value' is not supported")
