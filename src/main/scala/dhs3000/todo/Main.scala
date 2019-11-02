package dhs3000.todo

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import dhs3000.todo.server.HttpServer

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = HttpServer[IO]().as(ExitCode.Success)
}
