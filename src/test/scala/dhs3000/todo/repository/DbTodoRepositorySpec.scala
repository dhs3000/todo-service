package dhs3000.todo.repository

import cats.effect.IO
import dhs3000.todo.RunIO
import dhs3000.todo.model._
import dhs3000.todo.model.write._
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.scalatest.BeforeAndAfterEach

class DbTodoRepositorySpec extends RepositorySpec with BeforeAndAfterEach {

  describe("The DbTodoRepository") {
    val user1      = UserName("test-user-1")
    val user2      = UserName("test-user-2")
    val user1Todo  = Todo(user1, Title("test-title-u-1-1"), None)
    val user1Todo2 = Todo(user1, Title("test-title-u-1-2"), Some("Super duper"))
    val user2Todo  = Todo(user2, Title("test-title-u-2-1"), None)

    describe("can insert a new todo") {

      it("for one user") {
        RunIO {
          withTransactor { implicit xa =>
            for {
              _      <- repo.insertTodo(user1Todo)
              result <- repo.findAllByUserName(user1)
            } yield {

              result.map(r => (r.title, r.description)) must contain theSameElementsAs Seq(
                (user1Todo.title, user1Todo.description)
              )
            }
          }
        }
      }

      it("for several users") {
        RunIO {
          withTransactor { implicit xa =>
            for {
              _      <- repo.insertTodo(user1Todo)
              _      <- repo.insertTodo(user2Todo)
              result <- repo.findAllByUserName(user1)
            } yield {

              result.map(r => (r.title, r.description)) must contain theSameElementsAs Seq(
                (user1Todo.title, user1Todo.description)
              )
            }
          }
        }
      }

      it("several todos for one users") {
        RunIO {
          withTransactor { implicit xa =>
            for {
              _      <- repo.insertTodo(user1Todo)
              _      <- repo.insertTodo(user1Todo2)
              result <- repo.findAllByUserName(user1)
            } yield {

              result.map(r => (r.title, r.description)) must contain theSameElementsAs Seq(
                (user1Todo.title, user1Todo.description),
                (user1Todo2.title, user1Todo2.description)
              )
            }
          }
        }
      }
    }

    describe("can complete a todo") {
      it("updates the todo with the given ID") {
        RunIO {
          withTransactor { implicit xa =>
            for {
              _             <- repo.insertTodo(user1Todo)
              allTodos      <- repo.findAllByUserName(user1)
              result        <- repo.updateCompleted(allTodos(0).id, completed = true)
              allTodosAfter <- repo.findAllByUserName(user1)
            } yield {

              all(allTodos.map(_.completed)) must be(false)
              result must be(true)
              all(allTodosAfter.map(_.completed)) must be(true)
            }
          }
        }
      }

      it("updates the todo with the given ID but no other todos are affected") {
        RunIO {
          withTransactor { implicit xa =>
            for {
              _             <- repo.insertTodo(user1Todo)
              _             <- repo.insertTodo(user1Todo2)
              allTodos      <- repo.findAllByUserName(user1)
              result        <- repo.updateCompleted(allTodos(0).id, completed = true)
              allTodosAfter <- repo.findAllByUserName(user1)
            } yield {

              all(allTodos.map(_.completed)) must be(false)
              result must be(true)

              allTodosAfter must have size 2
              allTodosAfter(0).completed must be(true)
              allTodosAfter(1).completed must be(false)
            }
          }
        }
      }

      it("but returns false if no todo with the given ID exists") {
        RunIO {
          withTransactor { implicit xa =>
            for {
              _             <- repo.insertTodo(user1Todo)
              allTodos      <- repo.findAllByUserName(user1)
              result        <- repo.updateCompleted(TodoId(-1), completed = true)
              allTodosAfter <- repo.findAllByUserName(user1)
            } yield {

              all(allTodos.map(_.completed)) must be(false)
              result must be(false)
              all(allTodosAfter.map(_.completed)) must be(false)
            }
          }
        }
      }

      it("updates the todo with the given ID that was completed back to open") {
        RunIO {
          withTransactor { implicit xa =>
            for {
              _             <- repo.insertTodo(user1Todo)
              allTodos      <- repo.findAllByUserName(user1)
              result1       <- repo.updateCompleted(allTodos(0).id, completed = true)
              result2       <- repo.updateCompleted(allTodos(0).id, completed = false)
              allTodosAfter <- repo.findAllByUserName(user1)
            } yield {

              all(allTodos.map(_.completed)) must be(false)
              result1 must be(true)
              result2 must be(true)
              all(allTodosAfter.map(_.completed)) must be(false)
            }
          }
        }
      }
    }
  }

  private def repo(implicit xa: Transactor[IO]) = new DbTodoRepository[IO](xa)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    RunIO {
      withTransactor { implicit xa =>
        for {
          _ <- sql"delete from todo".update.run.transact(xa)
        } yield ()
      }
    }
  }
}
