package dhs3000.todo.http

import cats.Monad
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
import TodoHttpRoutes._

class TodoHttpRoutes[F[_]: Sync](todoService: TodoService[F]) extends Http4sDsl[F] {

  implicit private val http4sDsl: Http4sDsl[F] = this

  implicit private val todoEntityDecoder: EntityDecoder[F, write.UnvalidatedTodo]                    = jsonOf[F, write.UnvalidatedTodo]
  implicit private val updateCompletedTodoEntityDecoder: EntityDecoder[F, write.UpdateCompletedTodo] = jsonOf[F, write.UpdateCompletedTodo]

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root / username =>
      username.validated[UserName] { userName =>
        for {
          result   <- todoService.findAll(userName)
          response <- result.fold(handleError, x => Ok(x.asJson))
        } yield response
      }

    case req @ POST -> Root / username =>
      req.extract[write.UnvalidatedTodo].and(username).validated[write.Todo] { todo =>
        for {
          _        <- todoService.createNewTodo(todo)
          response <- Ok(todo.asJson)
        } yield response
      }

    case req @ PATCH -> Root / LongVar(id) =>
      req.decode[write.UpdateCompletedTodo] { updateCompletedTodo =>
        for {
          result   <- todoService.updateCompleted(TodoId(id), updateCompletedTodo.completed)
          response <- result.fold(handleError, _ => Ok())
        } yield response
      }
  }

  private def handleError(error: ServiceError): F[Response[F]] = error match {
    case UserNotFound(v) => NotFound(Seq(s"User not found ${v.value}").asJson)
    case TodoNotFound(v) => NotFound(Seq(s"Todo not found ${v.value}").asJson)
  }
}

private object TodoHttpRoutes extends {
  type GenerateResponse[A, F[_]] = (A => F[Response[F]]) => F[Response[F]]

  implicit class RichRequest[F[_]](val req: Request[F]) extends AnyVal {
    def extract[A](implicit F: Monad[F], decoder: EntityDecoder[F, A]): (A => F[Response[F]]) => F[Response[F]] =
      req.decode[A](_)
  }

  implicit class RichGenerateValidatableResponse[Unvalidated, F[_]](val unvalidated: GenerateResponse[Unvalidated, F]) extends AnyVal {
    def validated[Validated](createResponse: Validated => F[Response[F]])(
        implicit
        validation: Validation[Unvalidated, Validated],
        http4sDsl: Http4sDsl[F],
        F: Monad[F]
    ): F[Response[F]] =
      unvalidated(_.validated[Validated](createResponse))

    def and[OtherUnvalidated](otherUnvalidated: OtherUnvalidated): GenerateResponse[(Unvalidated, OtherUnvalidated), F] = {
      def combined(f: ((Unvalidated, OtherUnvalidated)) => F[Response[F]]): F[Response[F]] =
        unvalidated { uv =>
          f(uv, otherUnvalidated)
        }
      combined
    }
  }

  implicit class RichValidatable[Unvalidated, F[_]](val unvalidated: Unvalidated) extends AnyVal {
    def validated[Validated](createResponse: Validated => F[Response[F]])(
        implicit
        validation: Validation[Unvalidated, Validated],
        http4sDsl: Http4sDsl[F],
        F: Monad[F]
    ): F[Response[F]] = {
      import http4sDsl._
      validation
        .validate(unvalidated)
        .fold(
          errors => BadRequest(errors.asJson),
          validated => createResponse(validated)
        )
    }
  }
}
