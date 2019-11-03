package dhs3000.todo.server

import cats.effect.{Async, ContextShift}
import dhs3000.todo.config.Config
import dhs3000.todo.http.TodoHttpRoutes
import dhs3000.todo.repository.{DbTodoRepository, TodoRepository}
import dhs3000.todo.service.{TodoService, TodoServiceImpl}
import doobie.util.transactor.Transactor
import org.http4s.implicits._
import org.http4s.{HttpApp, HttpRoutes}

final class Application[F[_]: Async: ContextShift](implicit config: Config) {

  private val xa: Transactor[F] = {
    val db = config.dbConfig
    Transactor.fromDriverManager[F](
      db.driverName.value,
      db.jdbcUrl.value,
      db.username.value,
      db.password.value
    )
  }

  private val todoRepository: TodoRepository[F] = new DbTodoRepository[F](xa)
  private val todoService: TodoService[F]       = new TodoServiceImpl[F](todoRepository)
  private val httpRoutes: HttpRoutes[F]         = new TodoHttpRoutes[F](todoService).routes

  val httpApp: HttpApp[F] = httpRoutes.orNotFound
}
