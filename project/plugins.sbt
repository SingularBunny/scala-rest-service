resolvers ++= Seq(
  "JBoss" at "https://repository.jboss.org",
  "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Artima Maven Repository" at "http://repo.artima.com/releases"
)

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")
addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.0")