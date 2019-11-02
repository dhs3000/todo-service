import sbt._

object Versions {
  lazy val CatsEffect = "1.4.0"
  lazy val Http4s     = "0.20.11"
  lazy val Circe      = "0.12.3"
  lazy val Doobie     = "0.8.4"
  lazy val H2         = "1.4.199"
  lazy val Flyway     = "6.0.3"
  lazy val PureConfig = "0.12.1"
  lazy val Logback    = "1.2.3"
  lazy val ScalaTest  = "3.0.8"
}

object Dependencies {
  private val cats = Seq(
    "org.typelevel" %% "cats-effect" % Versions.CatsEffect
  )

  private val model = Seq(
    "eu.timepit" %% "refined" % "0.9.10"
  )

  private val webservice = Seq(
    "org.http4s" %% "http4s-blaze-server" % Versions.Http4s,
    "org.http4s" %% "http4s-circe"        % Versions.Http4s,
    "org.http4s" %% "http4s-dsl"          % Versions.Http4s,
    "io.circe"   %% "circe-core"          % Versions.Circe,
    "io.circe"   %% "circe-generic"       % Versions.Circe,
    "io.circe"   %% "circe-refined"       % Versions.Circe
  )

  private val db = Seq(
    "com.h2database" % "h2"               % Versions.H2,
    "org.flywaydb"   % "flyway-core"      % Versions.Flyway,
    "org.tpolecat"   %% "doobie-core"     % Versions.Doobie,
    "org.tpolecat"   %% "doobie-postgres" % Versions.Doobie,
    "org.tpolecat"   %% "doobie-h2"       % Versions.Doobie
  )

  private val config = Seq(
    "com.github.pureconfig" %% "pureconfig"             % Versions.PureConfig,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % Versions.PureConfig
  )

  private val logging = Seq(
    "ch.qos.logback" % "logback-classic" % Versions.Logback
  )

  private val test = Seq(
    "org.scalatest"  %% "scalatest"        % Versions.ScalaTest % Test,
    "org.scalacheck" %% "scalacheck"       % "1.14.2"           % Test,
    "org.tpolecat"   %% "doobie-scalatest" % Versions.Doobie    % Test
  )

  final val all = cats ++ model ++ webservice ++ db ++ config ++ logging ++ test
}
