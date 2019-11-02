package dhs3000.todo.server

import cats.effect.{Async, ConcurrentEffect, ContextShift, Timer}
import cats.implicits._
import pureconfig.generic.auto._
import dhs3000.todo.config.Config
import dhs3000.todo.initialization.Init
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import pureconfig._
import pureconfig.module.catseffect._

class HttpServer[F[_]: ConcurrentEffect: ContextShift: Timer] private (implicit config: Config, F: Async[F]) {

  private val init        = new Init[F]
  private val application = new Application[F]

  def serve: F[Unit] =
    for {
      _ <- initServer
      s <- createServer
    } yield s

  private def initServer = init.run

  private def createServer: F[Unit] =
    BlazeServerBuilder[F]
      .bindHttp(config.httpConfig.port.value, "0.0.0.0")
      .withHttpApp(Logger.httpApp(logHeaders = true, logBody = false)(application.httpApp))
      .serve
      .compile
      .drain
}

object HttpServer {
  def apply[F[_]: ConcurrentEffect: ContextShift: Timer](): F[Unit] =
    for {
      config <- ConfigSource.default.loadF[F, Config]
      server = create(config)
      s <- server.serve
    } yield s

  private def create[F[_]: ConcurrentEffect: ContextShift: Timer](config: Config) = {
    implicit val c: Config = config
    new HttpServer[F]
  }

}
