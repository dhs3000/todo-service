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

  implicit private val todoEntityDecoder: EntityDecoder[F, write.UnvalidatedTodo] = jsonOf[F, write.UnvalidatedTodo]

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

  private def handleError(error: ServiceError): F[Response[F]] = error match {
    case UserNotFound(u) => NotFound(Seq(s"User not found ${u.value}").asJson)
  }
}
