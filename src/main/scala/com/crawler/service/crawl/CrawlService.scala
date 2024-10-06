package com.crawler.service.crawl

import cats.Parallel
import cats.effect.kernel.Async
import cats.effect.syntax.concurrent.given
import cats.syntax.flatMap.given
import cats.syntax.functor.given
import com.crawler.config.crawl.CrawlConfig
import com.crawler.service.title.TitleService
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait CrawlService[F[_], P, R]:
  def crawl(rawUrls: List[P]): F[List[R]]

private final class CrawlServiceImpl[F[_]: Async: Parallel, P, R](
    log: Logger[F],
    config: CrawlConfig,
    titleService: TitleService[F, P, R]
) extends CrawlService[F, P, R]:
  override def crawl(rawUrls: List[P]): F[List[R]] =
    log.info(s"Start to process urls: ${rawUrls.mkString(", ")}") >>
      rawUrls.parTraverseN(config.maxPoolRequests)(titleService.getTitle)

object CrawlServiceImpl:
  def make[F[_]: Async: Parallel, P, R](
      config: CrawlConfig,
      titleService: TitleService[F, P, R]
  ): F[CrawlService[F, P, R]] =
    Slf4jLogger
      .fromName("CrawlServiceImpl")
      .map(CrawlServiceImpl(_, config, titleService))
