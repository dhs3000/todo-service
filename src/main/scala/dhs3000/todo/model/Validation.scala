package dhs3000.todo.model

import cats.data.ValidatedNec
import cats.syntax.apply._
import cats.syntax.validated._
import dhs3000.todo.model.Validation.Result
import dhs3000.todo.model.write.UnvalidatedTodo

trait Validation[Unvalidated, Validated] {
  def validate(unvalidated: Unvalidated): Result[Validated]
}

object Validation {
  type Result[A] = ValidatedNec[String, A]

  private val ValidUserName = "^([a-zA-Z0-9]+)$".r

  def validateUsername(username: String): Result[UserName] =
    username match {
      case ValidUserName(name) => UserName.from(name).fold(_.invalidNec, _.validNec)
      case _                   => s"Invalid username '$username'".invalidNec
    }

  def validateTitle(title: String): Result[Title] = Title.from(title).fold(_.invalidNec, _.validNec)

  def validateNewTodo(username: String, todo: write.UnvalidatedTodo): Result[write.Todo] = {
    val userName = validateUsername(username)
    val title    = validateTitle(todo.title)
    (userName, title).mapN(write.Todo(_, _, todo.description))
  }

  implicit val _1: Validation[String, UserName] = validateUsername
  implicit val _2: Validation[(UnvalidatedTodo, String), write.Todo] = {
    case (todo, username) => validateNewTodo(username, todo)
  }
}
