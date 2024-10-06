package com.crawler.service.parse

import cats.effect.kernel.Sync
import cats.syntax.flatMap.given
import cats.syntax.functor.given
import com.crawler.domain.parser.TitleNotFound
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait Parser[F[_], P]:
  def parse(value: P): F[String]

private final class HtmlTitleParser[F[_]: Sync](
    log: Logger[F],
    browser: Browser
) extends Parser[F, String]:
  override def parse(rawDoc: String): F[String] =
    for
      _        <- log.debug(s"Try to get title from $rawDoc")
      document <- Sync[F].delay(browser.parseString(rawDoc))
      title    <- Sync[F].delay(document.title)
      _        <- Sync[F].raiseWhen(title.isEmpty)(TitleNotFound)
    yield title

object HtmlTitleParser:
  def make[F[_]: Sync](): F[Parser[F, String]] =
    for
      log     <- Slf4jLogger.fromName("HtmlTitleParser")
      browser <- Sync[F].delay(JsoupBrowser())
    yield HtmlTitleParser(log, browser)
