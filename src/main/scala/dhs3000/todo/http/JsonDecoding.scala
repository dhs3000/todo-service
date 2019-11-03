package dhs3000.todo.http

import dhs3000.todo.model.write
import io.circe.Decoder

object JsonDecoding {

  implicit val unvalidatedTodoDecoder: Decoder[write.UnvalidatedTodo] =
    Decoder.forProduct2("title", "description")(write.UnvalidatedTodo)

  implicit val updateCompletedTodoDecoder: Decoder[write.UpdateCompletedTodo] =
    Decoder.forProduct1("completed")(write.UpdateCompletedTodo)

}
