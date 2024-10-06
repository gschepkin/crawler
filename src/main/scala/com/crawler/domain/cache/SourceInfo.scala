package com.crawler.domain.cache

import cats.syntax.either.given
import io.circe.{Decoder, Encoder}
import sttp.tapir.Schema

enum SourceInfo(val value: String):
  case LoadByUrl     extends SourceInfo("load_by_url")
  case LoadFromCache extends SourceInfo("load_from_cache")

object SourceInfo:
  given Encoder[SourceInfo] = Encoder.encodeString.contramap(_.value)
  given Decoder[SourceInfo] = Decoder.decodeString.emap(fromString(_).leftMap(_.getMessage))
  given Schema[SourceInfo]  = Schema.derived

  private def fromString(value: String): Either[NotSupportedCacheStatus, SourceInfo] =
    value.trim.toLowerCase match
      case SourceInfo.LoadByUrl.value     => SourceInfo.LoadByUrl.asRight[NotSupportedCacheStatus]
      case SourceInfo.LoadFromCache.value => SourceInfo.LoadFromCache.asRight[NotSupportedCacheStatus]
      case value                          => NotSupportedCacheStatus(value).asLeft[SourceInfo]

  final case class NotSupportedCacheStatus(value: String) extends IllegalArgumentException(s"GotStatus '$value' is not supported")
