name := "WebCrawlerAPI"

version := "1.0"

lazy val `webcrawlerapi` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws)

libraryDependencies += "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.15"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  