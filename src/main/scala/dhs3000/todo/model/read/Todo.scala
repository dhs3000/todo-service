package dhs3000.todo.model.read

import dhs3000.todo.model._

final case class Todo(id: TodoId, title: Title, description: Option[String], completed: Boolean)
