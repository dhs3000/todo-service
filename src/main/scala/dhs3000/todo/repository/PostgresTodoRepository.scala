package dhs3000.todo.repository

import cats.effect.Async
import cats.implicits._
import dhs3000.todo.model.{read, write}
import dhs3000.todo.model._
import doobie.implicits._
import doobie.util.{Get, Put, Read}
import doobie.util.transactor.Transactor

final class PostgresTodoRepository[F[_]: Async](xa: Transactor[F]) extends TodoRepository[F] {
  implicit val titleGet: Get[Title]       = Get[String].tmap(Title.unsafeFrom)
  implicit val titlePut: Put[Title]       = Put[String].tcontramap(_.value)
  implicit val userNamePut: Put[UserName] = Put[String].tcontramap(_.value)

  override def insertTodo(todo: write.Todo): F[Unit] = {
    val insert =
      sql"insert into todo (username, title, description) values(${todo.username}, ${todo.title}, ${todo.description})".update
    for {
      _ <- insert.run.transact(xa)
    } yield ()
  }

  override def findAllByUserName(userName: UserName): F[Vector[read.Todo]] =
    sql"SELECT id, title, description FROM todo WHERE username=${userName.value}"
      .query[read.Todo]
      .stream
      .transact(xa)
      .compile
      .toVector

}
