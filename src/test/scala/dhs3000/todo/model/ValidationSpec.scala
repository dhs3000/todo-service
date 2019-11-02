package dhs3000.todo.model

import dhs3000.todo.model.Validation.Result
import org.scalatest.FunSuite
import org.scalatest.prop.TableFor1
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ValidationSpec extends FunSuite with ScalaCheckPropertyChecks {

  new ValidationSpec(Validation.validateUsername)(userName).create()
  new ValidationSpec(Validation.validateTitle)(title).create()
  new ValidationSpec((in: (String, write.UnvalidatedTodo)) => Validation.validateNewTodo(in._1, in._2))(newTodo).create()

  class ValidationSpec[A, R](validate: A => Result[R])(fixture: ValidationFixture[A]) {
    def create(): Unit = {
      forAll(fixture.validExamples(fixture.name)) { example: A =>
        test(s"valid ${fixture.name} $example") {
          assert(validate(example).isValid)
        }
      }

      forAll(fixture.invalidExamples(fixture.name)) { example: A =>
        test(s"invalid ${fixture.name} $example") {
          assert(validate(example).isInvalid)
        }
      }
    }
  }

  abstract class ValidationFixture[A](val name: String) {
    def validExamples: String => TableFor1[A]
    def invalidExamples: String => TableFor1[A]
  }

  object userName extends ValidationFixture[String]("userName") {

    val validExamples =
      Table(
        _,
        "deho",
        "dhs3000",
        "x" * 20
      )

    val invalidExamples =
      Table(
        _,
        "",
        "test@local",
        "x" * 21,
      )
  }

  object title extends ValidationFixture[String]("title") {

    val validExamples =
      Table(
        _,
        "I can do this",
        "Never",
        "x" * 500
      )

    val invalidExamples =
      Table(
        _,
        "",
        "x" * 501,
      )
  }

  object newTodo extends ValidationFixture[(String, write.UnvalidatedTodo)]("newTodo") {

    val validExamples =
      Table(
        _,
        ("dhs3000", write.UnvalidatedTodo("I can do this", None)),
        ("x" * 20, write.UnvalidatedTodo("x" * 500, Some("this is a lot")))
      )

    val invalidExamples =
      Table(
        _,
        ("", write.UnvalidatedTodo("I can do this", None)),
        ("deho", write.UnvalidatedTodo("", None)),
        ("x" * 21, write.UnvalidatedTodo("x" * 500, Some("this is a lot"))),
        ("x" * 20, write.UnvalidatedTodo("x" * 501, Some("this is a lot"))),
      )
  }
}
