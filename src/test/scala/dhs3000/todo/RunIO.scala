package dhs3000.todo

import cats.effect.IO

object RunIO {
  def apply[A](ioa: IO[A]): A = ioa.unsafeRunSync()
}
