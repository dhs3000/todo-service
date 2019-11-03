package dhs3000.todo.repository

import cats.effect.Async
import cats.implicits._
import dhs3000.todo.model.{read, write, _}
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.{Get, Put}

final class DbTodoRepository[F[_]: Async](xa: Transactor[F]) extends TodoRepository[F] {
  implicit val titleGet: Get[Title]       = Get[String].tmap(Title.unsafeFrom)
  implicit val titlePut: Put[Title]       = Put[String].tcontramap(_.value)
  implicit val userNamePut: Put[UserName] = Put[String].tcontramap(_.value)

  override def insertTodo(todo: write.Todo): F[Unit] = {
    val insert =
      sql"INSERT INTO todo (username, title, description, completed) VALUES (${todo.username}, ${todo.title}, ${todo.description}, false)".update
    for {
      _ <- insert.run.transact(xa)
    } yield ()
  }

  override def updateCompleted(id: TodoId, completed: Boolean): F[Boolean] = {
    val update =
      sql"UPDATE todo SET completed = $completed where id = $id".update
    for {
      res <- update.run.transact(xa)
    } yield res >= 1
  }

  override def findAllByUserName(userName: UserName): F[Vector[read.Todo]] =
    sql"SELECT id, title, description, completed FROM todo WHERE username=${userName.value} ORDER BY id"
      .query[read.Todo]
      .stream
      .transact(xa)
      .compile
      .toVector

}
