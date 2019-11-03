package dhs3000.todo

import eu.timepit.refined._
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection._

package object model {

  type UserName = String Refined And[NonEmpty, MaxSize[W.`20`.T]]
  object UserName extends RefinedTypeOps[UserName, String]

  type Title = String Refined And[NonEmpty, MaxSize[W.`500`.T]]
  object Title extends RefinedTypeOps[Title, String]

  final case class TodoId(value: Long) extends AnyVal
}
