ThisBuild / scalaVersion := Versions.scala

lazy val settings = Seq(
  Compile / run / fork             := true,
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", "services", _*) => MergeStrategy.first
    case PathList("cache", _*)                => MergeStrategy.discard
    case PathList("META-INF", _*)             => MergeStrategy.discard
    case _                                    => MergeStrategy.first
  },
  developers +=
    Developer(
      "gschepkin",
      "Egor Schepkin",
      "eshepkin@gmail.com",
      url("https://github.com/gschepkin")
    )
)

lazy val crawler =
  (project in file("."))
    .settings(libraryDependencies ++= Dependencies.All)
    .settings(settings)
