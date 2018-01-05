name := "AGH_FEM"

version := "0.1"

scalaVersion := "2.11.8"
val nd4jVersion = "0.7.2"

libraryDependencies ++= Seq(
  "org.nd4j"             % "nd4j-native-platform" % nd4jVersion,
  "org.nd4j"            %% "nd4s"                 % nd4jVersion,
  "org.scalactic"       %% "scalactic"            % "3.0.4",
  "org.scalatest"       %% "scalatest" % "3.0.4"  % "test",
  "com.google.code.gson" % "gson"                 % "2.8.0",
"org.scalanlp" %% "breeze" % "0.13.2",

// Native libraries are not included by default. add this if you want them (as of 0.7)
// Native libraries greatly improve performance, but increase jar sizes.
// It also packages various blas implementations, which have licenses that may or may not
// be compatible with the Apache License. No GPL code, as best I know.
"org.scalanlp" %% "breeze-natives" % "0.13.2",

// The visualization library is distributed separately as well.
// It depends on LGPL code
"org.scalanlp" %% "breeze-viz" % "0.13.2"
)