package dhs3000.todo.config

import dhs3000.todo.config.DbConfig.{DriverName, JdbcUrl, Password, Username}
import dhs3000.todo.config.HttpConfig.Port

final case class Config(httpConfig: HttpConfig, dbConfig: DbConfig)

final case class HttpConfig(
    port: Port
)

object HttpConfig {
  final case class Port(value: Int) extends AnyVal
}

final case class DbConfig(
    driverName: DriverName,
    jdbcUrl: JdbcUrl,
    username: Username,
    password: Password
)
object DbConfig {
  final case class DriverName(value: String) extends AnyVal
  final case class JdbcUrl(value: String)    extends AnyVal
  final case class Username(value: String)   extends AnyVal
  final case class Password(value: String)   extends AnyVal
}
