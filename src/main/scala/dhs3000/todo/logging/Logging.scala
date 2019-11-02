package dhs3000.todo.logging

import cats.effect.Sync
import org.log4s.Logger

trait Logging {

  protected final def logInfo[F[_]](msg: => String)(implicit logger: Logger, F: Sync[F]): F[Unit] =
    F.delay(logger.info(msg))
}
