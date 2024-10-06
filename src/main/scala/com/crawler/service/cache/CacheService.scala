package com.crawler.service.cache

import cats.effect.kernel.{Async, Resource, Sync}
import cats.effect.syntax.resource.given
import cats.effect.syntax.spawn.given
import cats.syntax.applicativeError.given
import cats.syntax.flatMap.given
import cats.syntax.functor.given
import com.crawler.config.cache.{CacheConfig, CleanerConfig}
import com.crawler.database.repository.TitleRepository
import com.crawler.domain.cache.CacheResult
import com.crawler.domain.cache.SourceInfo.{LoadByUrl, LoadFromCache}
import com.crawler.domain.title.TitleResult
import com.crawler.service.title.{DefaultTitleService, TitleService}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.Instant
import scala.concurrent.duration.FiniteDuration

type CacheTitleService[F[_]] = TitleService[F, String, CacheResult[TitleResult]]

private final class TitleCacheServiceImpl[F[_]: Sync](
    log: Logger[F],
    config: CacheConfig,
    titleService: DefaultTitleService[F],
    titleRepository: TitleRepository[F]
) extends CacheTitleService[F]:

  override def getTitle(rawUri: String): F[CacheResult[TitleResult]] =
    def getFinalTtl(ttl: FiniteDuration) =
      Sync[F].delay(Instant.now().plusSeconds(ttl.toSeconds))

    def loadTitle =
      titleService.getTitle(rawUri).flatMap {
        case res: TitleResult.Success =>
          getFinalTtl(config.titleTtl.success).flatMap {
            titleRepository.insert(res, _)
          }.as(res)

        case res: TitleResult.Failure =>
          getFinalTtl(config.titleTtl.failure).flatMap {
            titleRepository.insert(res, _)
          }.as(res)
      }

    titleRepository.select(rawUri).flatMap {
      case Some(cachedValue) if config.enabled =>
        log.info(s"Got cached value for $rawUri - $cachedValue").as {
          CacheResult(result = cachedValue, source = LoadFromCache)
        }

      case _ =>
        loadTitle.map { title =>
          CacheResult(result = title, source = LoadByUrl)
        }
    }

object TitleCacheServiceImpl:
  def cleaner[F[_]: Async](config: CleanerConfig, titleRepository: TitleRepository[F]): Resource[F, Unit] =
    Slf4jLogger
      .fromName[F]("TitleCacheServiceCleaner")
      .toResource
      .flatMap { log =>
        fs2.Stream
          .awakeEvery[F](config.awakeEvery)
          .evalMap[F, Unit] { _ =>
            val deleting =
              Sync[F]
                .delay(Instant.now())
                .flatMap(
                  titleRepository.deleteOlderThan
                )

            deleting.attempt.flatMap {
              case Right(_)    => log.debug("Successfully deleted")
              case Left(error) => log.error(error)(s"Error while removing cache data: ${error.getMessage}")
            }
          }
          .compile
          .drain
          .background
          .void
      }

  def make[F[_]: Async](
      config: CacheConfig,
      titleService: DefaultTitleService[F],
      titleRepository: TitleRepository[F]
  ): F[CacheTitleService[F]] =
    Slf4jLogger
      .fromName("TitleCacheService")
      .map(TitleCacheServiceImpl(_, config, titleService, titleRepository))
