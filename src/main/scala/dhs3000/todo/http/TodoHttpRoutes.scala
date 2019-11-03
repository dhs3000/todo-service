package dhs3000.todo.http

import cats.effect._
import cats.syntax.flatMap._
import cats.syntax.functor._
import dhs3000.todo.http.JsonDecoding._
import dhs3000.todo.http.JsonEncoding._
import dhs3000.todo.model._
import dhs3000.todo.service.TodoService
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class TodoHttpRoutes[F[_]: Sync](todoService: TodoService[F]) extends Http4sDsl[F] {

  implicit private val todoEntityDecoder: EntityDecoder[F, write.UnvalidatedTodo]                    = jsonOf[F, write.UnvalidatedTodo]
  implicit private val updateCompletedTodoEntityDecoder: EntityDecoder[F, write.UpdateCompletedTodo] = jsonOf[F, write.UpdateCompletedTodo]

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root / username =>
      Validation
        .validateUsername(username)
        .fold(
          errors => BadRequest(errors.asJson),
          validUserName => findAll(validUserName)
        )

    case req @ POST -> Root / username =>
      req.decode[write.UnvalidatedTodo] { unvalidatedTodo =>
        Validation
          .validateNewTodo(username, unvalidatedTodo)
          .fold(
            errors => BadRequest(errors.asJson),
            todo => createNewTodo(todo)
          )
      }

    case req @ PATCH -> Root / LongVar(id) =>
      req.decode[write.UpdateCompletedTodo] { updateCompletedTodo =>
        updateCompleted(TodoId(id), updateCompletedTodo.completed)
      }
  }

  private def findAll(userName: UserName) =
    for {
      result   <- todoService.findAll(userName)
      response <- result.fold(handleError, x => Ok(x.asJson))
    } yield response

  private def createNewTodo(todo: write.Todo) =
    for {
      _        <- todoService.createNewTodo(todo)
      response <- Ok(todo.asJson)
    } yield response

  private def updateCompleted(id: TodoId, completed: Boolean) =
    for {
      result   <- todoService.updateCompleted(id, completed)
      response <- result.fold(handleError, x => Ok())
    } yield response

  private def handleError(error: ServiceError): F[Response[F]] = error match {
    case UserNotFound(v) => NotFound(Seq(s"User not found ${v.value}").asJson)
    case TodoNotFound(v) => NotFound(Seq(s"Todo not found ${v.value}").asJson)
  }
}
