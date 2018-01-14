name := "AGH_FEM"

version := "0.1"

scalaVersion := "2.11.8"
val nd4jVersion = "0.7.2"

libraryDependencies ++= Seq(
  "org.nd4j"             % "nd4j-native-platform" % nd4jVersion,
  "org.nd4j"            %% "nd4s"                 % nd4jVersion,
  "org.scalactic"       %% "scalactic"            % "3.0.4",
  "org.scalatest"       %% "scalatest"            % "3.0.4"  % "test",
  "com.google.code.gson" % "gson"                 % "2.8.0",
  "org.scalanlp"        %% "breeze"               % "0.13.2",
  "com.typesafe.play"   %% "play-json"            % "2.6.8",
  "org.scalanlp"        %% "breeze-natives"       % "0.13.2"
)