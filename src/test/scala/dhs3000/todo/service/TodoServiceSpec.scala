package dhs3000.todo.service

import cats.data.EitherT
import cats.effect.IO
import dhs3000.todo.RunIO
import dhs3000.todo.model._
import dhs3000.todo.repository.TodoRepository
import org.scalatest.{FunSpec, MustMatchers}

class TodoServiceSpec extends FunSpec with MustMatchers {

  describe("The TodoService") {

    // TODO test create new todo

    describe("can find all todos for a given username") {
      val expectedUserName = UserName("test-user")

      it("when some exist") {
        RunIO {
          val expectedTodos = Vector(
            read.Todo(TodoId(42), Title("Number one"), None, false),
            read.Todo(TodoId(66), Title("Number two"), Some("Meh"), true)
          )
          val repo = new TestTodoRepository(expectedTodos)

          EitherT(service(repo).findAll(expectedUserName)).map { result =>
            repo.lastRequestedUserName must be(expectedUserName)
            result.toVector must contain theSameElementsAs expectedTodos
          }.value
        }
      }

      it("but returns an error if none exists") {
        RunIO {
          val repo = new TestTodoRepository(Vector.empty)

          EitherT(service(repo).findAll(expectedUserName)).leftMap { result =>
            repo.lastRequestedUserName must be(expectedUserName)
            result must be(UserNotFound(expectedUserName))
          }.value
        }
      }
    }

    describe("can complete a todo with a given ID") {
      val expectedTodoId = TodoId(66)

      it("when a todo exists") {
        RunIO {
          val repo = new TestTodoRepository(completedTodoWasFound = true)

          EitherT(service(repo).updateCompleted(expectedTodoId, completed = true)).map { result =>
            repo.lastUpdateCompleteTodo must be(expectedTodoId -> true)
            result must be(())
          }.value
        }
      }

      it("but returns an error if none exists") {
        RunIO {
          val repo = new TestTodoRepository(completedTodoWasFound = false)

          EitherT(service(repo).updateCompleted(expectedTodoId, completed = true)).leftMap { result =>
            repo.lastUpdateCompleteTodo must be(expectedTodoId -> true)
            result must be(TodoNotFound(expectedTodoId))
          }.value
        }
      }
    }
  }

  private def service(repo: TodoRepository[IO]) = new TodoServiceImpl(repo)

  private class TestTodoRepository(
      todos: Vector[read.Todo] = Vector.empty,
      completedTodoWasFound: Boolean = false
  ) extends TodoRepository[IO] {
    var lastInsertedTodo: write.Todo              = _
    var lastUpdateCompleteTodo: (TodoId, Boolean) = _
    var lastRequestedUserName: UserName           = _

    override def insertTodo(todo: write.Todo): IO[Unit] = IO {
      lastInsertedTodo = todo
    }

    override def findAllByUserName(userName: UserName): IO[Vector[read.Todo]] = IO {
      lastRequestedUserName = userName
      todos
    }

    override def updateCompleted(id: TodoId, completed: Boolean): IO[Boolean] = IO {
      lastUpdateCompleteTodo = id -> completed
      completedTodoWasFound
    }
  }
}
