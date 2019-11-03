package dhs3000.todo.repository

import cats.effect.IO
import dhs3000.todo.RunIO
import dhs3000.todo.model._
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.scalatest.BeforeAndAfterEach

class DbTodoRepositorySpec extends RepositorySpec with BeforeAndAfterEach {

  describe("The DbTodoRepository") {

    describe("can insert a new todo") {
      import dhs3000.todo.model.write._

      val user1      = UserName("test-user-1")
      val user2      = UserName("test-user-2")
      val user1Todo  = Todo(user1, Title("test-title-u-1-1"), None)
      val user1Todo2 = Todo(user1, Title("test-title-u-1-2"), Some("Super duper"))
      val user2Todo  = Todo(user2, Title("test-title-u-2-1"), None)

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
