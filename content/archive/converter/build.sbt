val CirceVersion      = "0.11.1"
val CatsVersion       = "1.6.1"
val CatsEffectVersion = "1.3.1"

name := "converter"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "io.circe"      %% "circe-core"    % CirceVersion,
  "io.circe"      %% "circe-generic" % CirceVersion,
  "io.circe"      %% "circe-parser"  % CirceVersion,
  "org.typelevel" %% "cats-core"     % CatsVersion,
  "org.typelevel" %% "cats-effect"   % CatsEffectVersion
)

scalafmtOnCompile := true
