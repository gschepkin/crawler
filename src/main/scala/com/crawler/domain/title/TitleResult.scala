package com.crawler.domain.title

import com.crawler.domain.server.HttpResponse
import com.crawler.domain.title.TitleResult.Success
import doobie.{Read, Write}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import sttp.tapir.Schema

sealed trait TitleResult

object TitleResult:
  given Encoder[TitleResult] =
    case res: Success => deriveEncoder[TitleResult.Success](res)
    case res: Failure => deriveEncoder[TitleResult.Failure](res)

  given Decoder[TitleResult] = cursor =>
    cursor.downField("title").as[Option[String]] match
      case Right(_) => deriveDecoder[TitleResult.Success](cursor)
      case _        => deriveDecoder[TitleResult.Failure](cursor)

  given Schema[TitleResult] = Schema.any

  given Read[TitleResult]  = Read[(String, String, GotStatus)].map {
    case (url, title, status @ GotStatus.Success) => Success(url, title, status)
    case (url, error, status @ GotStatus.Failure) => Failure(url, error, status)
  }
  given Write[TitleResult] = Write[(String, String, GotStatus)].contramap {
    case Success(url, title, status) => (url, title, status)
    case Failure(url, error, status) => (url, error, status)
  }

  final case class Success(
      url: String,
      title: String,
      status: GotStatus
  ) extends TitleResult

  object Success:
    given Encoder[Success] = deriveEncoder
    given Decoder[Success] = deriveDecoder
    given Schema[Success]  = Schema.derived

  final case class Failure(
      url: String,
      error: String,
      status: GotStatus
  ) extends TitleResult

  object Failure:
    given Encoder[Failure] = deriveEncoder
    given Decoder[Failure] = deriveDecoder
    given Schema[Failure]  = Schema.derived
