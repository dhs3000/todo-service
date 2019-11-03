package dhs3000.todo.http

import cats.data.NonEmptyVector
import cats.effect.IO
import cats.syntax.apply._
import dhs3000.todo.RunIO
import dhs3000.todo.model._
import dhs3000.todo.service.TodoService
import io.circe.Decoder
import io.circe.refined._
import org.http4s.circe._
import org.http4s.{EntityDecoder, HttpRoutes, Request, Response, Status, Uri}
import org.scalatest.{FunSpec, MustMatchers}

class TodoHttpRoutesSpec extends FunSpec with MustMatchers {
  // Todo test create new todo
  // Todo test update completed flag

  describe("The TodoHttpRoutes provides routes to") {

    describe("find all todos for a user, that") {
      val username = UserName("testuser")
      val request  = Request[IO](uri = Uri(path = s"/${username.value}"))

      it("returns the todos if some exists") {
        val todo1   = read.Todo(TodoId(42), Title("test-title-1"), None, false)
        val todo2   = read.Todo(TodoId(66), Title("test-title-2"), Some("test-description-2"), true)
        val service = new TestTodoService(Right(NonEmptyVector.of(todo1, todo2)))

        withResponse(service, request) { response =>
          IO(response.status must be(Status.Ok)) *>
            response.as[Array[read.Todo]].map(_ must be(Array(todo1, todo2))) *>
            IO(service.lastRequestedUserName must be(Some(username)))
        }
      }

      it("returns an error if no todos exist") {
        val service = new TestTodoService(Left(UserNotFound(username)))

        withResponse(service, request) { response =>
          IO(response.status must be(Status.NotFound)) *>
            response.as[String].map(_ must be("""["User not found testuser"]""")) *>
            IO(service.lastRequestedUserName must be(Some(username)))
        }
      }

      it("returns an error if the username isn't valid") {
        val invalidRequest = Request[IO](uri = Uri(path = s"/test-user"))
        val service        = new TestTodoService()

        withResponse(service, invalidRequest) { response =>
          IO(response.status must be(Status.BadRequest)) *>
            response.as[String].map(_ must be("""["Invalid username 'test-user'"]""")) *>
            IO(service.lastRequestedUserName must be(empty))
        }
      }

      def withResponse[A](service: TodoService[IO], request: Request[IO])(test: Response[IO] => IO[A]): Unit =
        RunIO {
          httpRoutes(service)(request).value.flatMap { response =>
            response
              .map(test)
              .getOrElse(IO(fail("Empty response")) *> IO.unit)
          }
        }
    }
  }

  private def httpRoutes(service: TodoService[IO]): HttpRoutes[IO] = new TodoHttpRoutes[IO](service).routes

  implicit val todoIdDecoder: Decoder[TodoId] = Decoder.decodeLong.map(TodoId)
  implicit val readTodoDecoder: Decoder[read.Todo] =
    Decoder.forProduct4("id", "title", "description", "completed")(read.Todo)

  implicit val readTodoJsonDecoder: EntityDecoder[IO, Array[read.Todo]] = jsonOf[IO, Array[read.Todo]]

  private class TestTodoService(todos: Either[UserNotFound, NonEmptyVector[read.Todo]] = Left(UserNotFound(UserName("dummy"))))
      extends TodoService[IO] {

    var lastInsertedTodo: Option[write.Todo]    = None
    var lastRequestedUserName: Option[UserName] = None

    override def createNewTodo(todo: write.Todo): IO[Unit] = IO {
      lastInsertedTodo = Some(todo)
    }

    override def updateCompleted(id: TodoId, completed: Boolean): IO[Either[TodoNotFound, Unit]] = ???

    override def findAll(userName: UserName): IO[Either[UserNotFound, NonEmptyVector[read.Todo]]] = IO {
      lastRequestedUserName = Some(userName)
      todos
    }
  }
}
