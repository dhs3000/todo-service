package dhs3000.todo.service

import cats.data.NonEmptyVector
import cats.effect.Async
import cats.syntax.functor._
import dhs3000.todo.model._
import dhs3000.todo.repository.TodoRepository

trait TodoService[F[_]] {
  def createNewTodo(todo: write.Todo): F[Unit]
  def findAll(userName: UserName): F[Either[ServiceError, NonEmptyVector[read.Todo]]]
}

final class TodoServiceImpl[F[_]: Async](todoRepo: TodoRepository[F]) extends TodoService[F] {

  override def createNewTodo(todo: write.Todo): F[Unit] = todoRepo.insertTodo(todo)

  override def findAll(userName: UserName): F[Either[ServiceError, NonEmptyVector[read.Todo]]] =
    for {
      result <- todoRepo.findAllByUserName(userName)
    } yield NonEmptyVector.fromVector(result).toRight(UserNotFound(userName))

}
