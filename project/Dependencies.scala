import sbt.*

object Dependencies {
  object Config {
    val pureConfig: ModuleID        = "com.github.pureconfig" %% "pureconfig-core"           % Versions.pureconfig
    val pureGenericConfig: ModuleID = "com.github.pureconfig" %% "pureconfig-generic-scala3" % Versions.pureconfig

    val all: Seq[ModuleID] = pureConfig :: pureGenericConfig :: Nil
  }

  object TypeLevel {
    val catsEffect: ModuleID = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    val fs2: ModuleID        = "co.fs2"        %% "fs2-core"    % Versions.fs2

    val all: Seq[ModuleID] = catsEffect :: fs2 :: Nil
  }

  object Parsing {
    val circeCore: ModuleID    = "io.circe"                      %% "circe-core"       % Versions.circe
    val circeParser: ModuleID  = "io.circe"                      %% "circe-parser"     % Versions.circe
    val circeGeneric: ModuleID = "io.circe"                      %% "circe-generic"    % Versions.circe
    val fs2: ModuleID          = "io.circe"                      %% "circe-fs2"        % Versions.circe
    val sttp: ModuleID         = "com.softwaremill.sttp.client4" %% "circe"            % Versions.sttp
    val tapir: ModuleID        = "com.softwaremill.sttp.tapir"   %% "tapir-json-circe" % Versions.tapir

    val scraper: ModuleID = "net.ruippeixotog" %% "scala-scraper" % Versions.scraper

    val json: Seq[ModuleID] = circeCore :: circeParser :: circeGeneric :: fs2 :: sttp :: tapir :: Nil

    val html: Seq[ModuleID] = scraper :: Nil

    val all: Seq[ModuleID] = json ++ html
  }

  object Http {
    val sttpFs2Client: ModuleID = "com.softwaremill.sttp.client4" %% "fs2"               % Versions.sttp
    val tapirSttp: ModuleID     = "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client" % Versions.tapir

    val tapirOpenApiDoc: ModuleID         = "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"      % Versions.tapir
    val tapirOpenApiDocGenerate: ModuleID = "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % Versions.tapir
    val tapirCore: ModuleID               = "com.softwaremill.sttp.tapir" %% "tapir-core"              % Versions.tapir
    val tapirServer: ModuleID             = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"     % Versions.tapir
    val http4sServer                      = "org.http4s"                  %% "http4s-ember-server"     % Versions.http4s

    val server: Seq[ModuleID] = tapirOpenApiDoc :: tapirOpenApiDocGenerate :: tapirCore :: tapirServer :: http4sServer :: Nil
    val client: Seq[ModuleID] = sttpFs2Client :: tapirSttp :: Nil
    val all: Seq[ModuleID]    = server ++ client
  }

  object Logging {
    val log4j: ModuleID         = "org.apache.logging.log4j" % "log4j-api"         % Versions.log4j
    val log4jCore: ModuleID     = "org.apache.logging.log4j" % "log4j-core"        % Versions.log4j
    val log4jImpl: ModuleID     = "org.apache.logging.log4j" % "log4j-slf4j2-impl" % Versions.log4j
    val slf4jSimple: ModuleID   = "org.slf4j"                % "slf4j-simple"      % Versions.slf4j
    val slf4jApi: ModuleID      = "org.slf4j"                % "slf4j-api"         % Versions.slf4j
    val log4catsCore: ModuleID  = "org.typelevel"           %% "log4cats-core"     % Versions.log4cats
    val log4catsSlf4j: ModuleID = "org.typelevel"           %% "log4cats-slf4j"    % Versions.log4cats

    val all: Seq[ModuleID] = slf4jApi :: log4j :: log4jCore :: log4jImpl :: log4catsCore :: log4catsSlf4j :: Nil
  }

  object Testing {
    val scalatest: ModuleID   = "org.scalatest" %% "scalatest"                     % Versions.scalatest         % Test
    val catsEffects: ModuleID = "org.typelevel" %% "cats-effect-testing-scalatest" % Versions.catsEffectTesting % Test

    val all: Seq[ModuleID] = scalatest :: catsEffects :: Nil
  }

  object DataBase {
    val doobieCore: ModuleID           = "org.tpolecat" %% "doobie-core"                % Versions.doobie
    val doobieHikari: ModuleID         = "org.tpolecat" %% "doobie-hikari"              % Versions.doobie
    val doobiePostgresDriver: ModuleID = "org.tpolecat" %% "doobie-postgres"            % Versions.doobie
    val flyway: ModuleID               = "org.flywaydb"  % "flyway-core"                % Versions.flyway
    val flywayPostgresql: ModuleID     = "org.flywaydb"  % "flyway-database-postgresql" % Versions.flyway

    val all: Seq[ModuleID] = doobieCore :: doobieHikari :: doobiePostgresDriver :: flyway :: flywayPostgresql :: Nil
  }

  val All: Seq[ModuleID] =
    TypeLevel.all ++
      Logging.all ++
      Config.all ++
      Http.all ++
      Parsing.all ++
      DataBase.all ++
      Testing.all
}
