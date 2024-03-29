package dhs3000.todo.model

sealed trait ServiceError extends Product with Serializable

final case class UserNotFound(username: UserName) extends ServiceError
final case class TodoNotFound(id: TodoId)         extends ServiceError
