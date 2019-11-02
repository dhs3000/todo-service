package dhs3000.todo.repository

import cats.effect._
import doobie.h2.H2Transactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterAll, FunSpecLike, MustMatchers}

trait RepositorySpec extends FunSpecLike with MustMatchers with BeforeAndAfterAll {

  // We need a ContextShift[IO] before we can construct a Transactor[IO]. The passed ExecutionContext
  // is where nonblocking operations will be executed. For testing here we're using a synchronous EC.
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  private val dbUrl  = "jdbc:h2:mem:users;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
  private val dbUser = "sa"
  private val dbPass = ""

  final protected def transactor: Resource[IO, H2Transactor[IO]] =
    H2Transactor.newH2Transactor[IO](
      dbUrl,
      dbUser,
      dbPass,
      ExecutionContexts.synchronous,
      Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
    )

  final protected def withTransactor[A](test: Transactor[IO] => IO[A]): IO[A] =
    transactor.use(test)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    createAndInitDb()
  }

  private def createAndInitDb(): Unit = {
    val flyway = Flyway.configure().dataSource(dbUrl, dbUser, dbPass).load()
    flyway.migrate()
  }
}
