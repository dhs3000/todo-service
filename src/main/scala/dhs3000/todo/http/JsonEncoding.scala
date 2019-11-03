package dhs3000.todo.http

import cats.data.{NonEmptyChain, NonEmptyVector}
import dhs3000.todo.model.{read, write, TodoId}
import io.circe.{Encoder, Json}
import io.circe.refined._
import io.circe.syntax._

object JsonEncoding {

  implicit val todoIdEncoder: Encoder[TodoId] = Encoder.instance { x =>
    Json.fromLong(x.value)
  }

  implicit val readTodoEncoder: Encoder[read.Todo] =
    Encoder.forProduct4("id", "title", "description", "completed")(d => (d.id, d.title, d.description, d.completed))

  implicit val writeTodoEncoder: Encoder[write.Todo] =
    Encoder.forProduct3("username", "title", "description")(d => (d.username, d.title, d.description))

  implicit def nevEncoder[A](implicit vecEncoder: Encoder[Vector[A]]): Encoder[NonEmptyVector[A]] = Encoder.instance { x =>
    x.toVector.asJson
  }

  implicit def necEncoder[A](implicit vecEncoder: Encoder[Vector[A]]): Encoder[NonEmptyChain[A]] = Encoder.instance { x =>
    x.toChain.toVector.asJson
  }
}
