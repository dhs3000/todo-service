package dhs3000.todo

import cats.data.{NonEmptyChain, NonEmptyVector}
import dhs3000.todo.model.read
import dhs3000.todo.model.write
import io.circe.Encoder
import io.circe.refined._
import io.circe.syntax._

package object http {

  implicit val readTodoEncoder: Encoder[read.Todo] =
    Encoder.forProduct3("id", "title", "description")(d => (d.id, d.title, d.description))

  implicit val writeTodoEncoder: Encoder[write.Todo] =
    Encoder.forProduct3("username", "title", "description")(d => (d.username, d.title, d.description))

  implicit def nevEncoder[A](implicit vecEncoder: Encoder[Vector[A]]): Encoder[NonEmptyVector[A]] = Encoder.instance { x =>
    x.toVector.asJson
  }

  implicit def necEncoder[A](implicit vecEncoder: Encoder[Vector[A]]): Encoder[NonEmptyChain[A]] = Encoder.instance { x =>
    x.toChain.toVector.asJson
  }

}
