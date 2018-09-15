val CirceVersion      = "0.9.3"
val CatsVersion       = "1.4.0"
val CatsEffectVersion = "1.0.0"

name := "converter"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "io.circe"      %% "circe-core"    % CirceVersion,
  "io.circe"      %% "circe-generic" % CirceVersion,
  "io.circe"      %% "circe-parser"  % CirceVersion,
  "org.typelevel" %% "cats-core"     % CatsVersion,
  "org.typelevel" %% "cats-effect"   % CatsEffectVersion
)

scalafmtOnCompile := true
