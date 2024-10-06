package com.crawler.service.title

import cats.effect.kernel.Sync
import cats.syntax.applicativeError.given
import cats.syntax.either.given
import cats.syntax.flatMap.given
import cats.syntax.functor.given
import com.crawler.domain.title.{GotStatus, TitleResult}
import com.crawler.service.client.Client
import com.crawler.service.parse.Parser
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.model.Uri

type DefaultTitleService[F[_]] = TitleService[F, String, TitleResult]

trait TitleService[F[_], P, R]:
  def getTitle(param: P): F[R]

private final class TitleServiceImpl[F[_]: Sync](
    log: Logger[F],
    client: Client[F],
    parser: Parser[F, String]
) extends DefaultTitleService[F]:
  override def getTitle(rawUri: String): F[TitleResult] =
    def parsedUri =
      Uri.parse(rawUri).leftMap(IllegalArgumentException(_))

    val title =
      for
        uri      <- Sync[F].fromEither(parsedUri)
        response <- client.get(uri)
        result   <- parser.parse(response)
      yield result

    log.debug(s"Try getting title for $rawUri") >>
      title.attempt.map {
        case Left(error) =>
          TitleResult.Failure(
            url = rawUri,
            error = error.getMessage,
            status = GotStatus.Failure
          )

        case Right(title) =>
          TitleResult.Success(
            url = rawUri,
            title = title,
            status = GotStatus.Success
          )
      }

object TitleServiceImpl:
  def make[F[_]: Sync](
      client: Client[F],
      parser: Parser[F, String]
  ): F[DefaultTitleService[F]] =
    Slf4jLogger
      .fromName("TitleService")
      .map(TitleServiceImpl(_, client, parser))
