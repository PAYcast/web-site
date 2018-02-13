val CirceVersion = "0.9.1"
val CatsVersion = "1.0.1"
val CatsEffectVersion = "0.8"

name := "converter"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "io.circe"      %% "circe-core"    % CirceVersion,
  "io.circe"      %% "circe-generic" % CirceVersion,
  "io.circe"      %% "circe-parser"  % CirceVersion,
  "org.typelevel" %% "cats-core"     % CatsVersion,
  "org.typelevel" %% "cats-effect"   % CatsEffectVersion,
)