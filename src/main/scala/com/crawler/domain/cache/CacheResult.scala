package com.crawler.domain.cache

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import sttp.tapir.Schema

final case class CacheResult[R](
    result: R,
    source: SourceInfo
)

object CacheResult:
  given [R: Encoder]: Encoder[CacheResult[R]] = deriveEncoder
  given [R: Decoder]: Decoder[CacheResult[R]] = deriveDecoder
  given [R: Schema]: Schema[CacheResult[R]]   = Schema.derived
