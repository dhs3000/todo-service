package dhs3000.todo.model.write

import dhs3000.todo.model._

final case class Todo(username: UserName, title: Title, description: Option[String])
