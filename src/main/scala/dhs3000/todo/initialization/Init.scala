package dhs3000.todo.initialization

import cats.effect.{Async, ContextShift}
import cats.implicits._
import dhs3000.todo.config.Config
import dhs3000.todo.logging.Logging
import org.flywaydb.core.Flyway
import org.log4s.{getLogger, Logger}

final class Init[F[_]: ContextShift](implicit config: Config, F: Async[F]) extends Logging {

  implicit private val logger: Logger = getLogger

  def run: F[Unit] =
    for {
      _ <- logInfo("Initializing the server")
      _ <- initDb
      _ <- logInfo("Initialized the server")
    } yield ()

  private def initDb: F[Unit] = F.delay {
    val db     = config.dbConfig
    val flyway = Flyway.configure().dataSource(db.jdbcUrl.value, db.username.value, db.password.value).load()
    flyway.migrate()
  }
}
