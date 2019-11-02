package dhs3000.todo.repository

import dhs3000.todo.model.write
import dhs3000.todo.model.read
import dhs3000.todo.model._

trait TodoRepository[F[_]] {
  def insertTodo(todo: write.Todo): F[Unit]
  def findAllByUserName(userName: UserName): F[Vector[read.Todo]]
}
