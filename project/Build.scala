import sbt._
import sbt.Keys._



object BuildSettings {
  import Dependencies._

  val buildOrganization ="slickfun"
  val buildVersion      = "1.0.0"
  val buildScalaVersion = "2.11.2"

  val buildSettings = Seq(
    organization := buildOrganization,
    version :=  buildScalaVersion,
    scalaVersion := buildScalaVersion,
    //crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.11.0", "2.11.1", "2.11.2"),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.sonatypeRepo("releases"),
    scalacOptions ++= Seq(),
    libraryDependencies ++= List( scalaTest, slf4s, logback )
  )
}

object Resolvers {
  val sunrepo    = "Sun Maven2 Repo" at "http://download.java.net/maven/2"
  val sunrepoGF  = "Sun GF Maven2 Repo" at "http://download.java.net/maven/glassfish"
  val oraclerepo = "Oracle Maven2 Repo" at "http://download.oracle.com/maven"

  val oracleResolvers = Seq (sunrepo, sunrepoGF, oraclerepo)
}

object Dependencies {
  val slickVersion = "2.1.0"
  val slickCodegenVersion = "2.1.0-RC3"

  val slf4jVersion = "1.6.4"
  val slf4sVersion = "1.7.7"
  val logbackVersion = "1.1.2"
  val scalaTestVersion = "2.2.1"
  val postgresJDBCVersion ="9.3-1102-jdbc4"
  val configsVersion = "0.2.2"
  val h2JDBCVersion = "1.4.185"
  val sqlliteJDBCVersion = "3.8.7"


  var scalaTest = "org.scalatest" % "scalatest_2.11" % scalaTestVersion % "test"

  val slick ="com.typesafe.slick" %% "slick" % slickVersion
  val slickCodeGen = "com.typesafe.slick" %% "slick-codegen" % slickCodegenVersion
  val postgresJDBC = "org.postgresql" % "postgresql" % postgresJDBCVersion
  val h2JDBC = "com.h2database" % "h2" % h2JDBCVersion
  val sqliteJDBC = "org.xerial" % "sqlite-jdbc" % sqlliteJDBCVersion

//  val slf4j = "org.slf4j" % "slf4j-nop" % slf4jVersion
  val slf4s = "org.slf4s" %% "slf4s-api" % slf4sVersion
  val logback = "ch.qos.logback" % "logback-classic" % logbackVersion

}

object SlickFunBuild extends Build {
  import BuildSettings._
  import Dependencies._

  val dbLogin = taskKey[String]("db login")
  val dbPasswd = taskKey[String]("db passwd")
  val dbUrl = taskKey[String]("db URL")
  val dbDriver = taskKey[String]("db driver")
  val dbSlickDriver = taskKey[String]("db driver")

  val slickGenPackage = taskKey[String]("slick generator package")

  lazy val root = Project(
    id="slickfun",
    base= file("."),
    settings=buildSettings
  ) aggregate(example1,example2,example3)

  /*
  lazy val db = Project(
    id="db",
    base= file("db"),
    settings = buildSettings ++ Seq(
      slickKey <<= slickCodeGenTask, // register manual sbt command
      sourceGenerators in Compile <+= slickCodeGenTask,  // register automatic code generation on every compile, remove for only manual use
      libraryDependencies ++= Seq( slick,slickCodeGen, slf4s, postgresJDBC )
    )
  ).dependsOn()
  */
  lazy val example1 = Project(
    id="example01",
    base= file("example01"),
    settings = buildSettings ++ (libraryDependencies ++= Seq( slick, slf4s, h2JDBC ) )
  )
  lazy val example2 = Project(
    id="example02",
    base= file("example02"),
    settings = buildSettings ++ (libraryDependencies ++= Seq( slick, slf4s, h2JDBC ) )
  )
  lazy val example3 = Project(
    id="example03",
    base= file("example03"),
    settings = buildSettings ++ Seq(
      slickKey <<= slickCodeGenTask, // register manual sbt command
      sourceGenerators in Compile <+= slickCodeGenTask,  // register automatic code generation on every compile, remove for only manual use
      libraryDependencies ++= Seq( slick,slickCodeGen, slf4s, sqliteJDBC)
    )
  )


  /*
  lazy val config = Project(
    id="config",
    base=file("config"),
    settings = buildSettings ++ ( libraryDependencies ++= Seq( configs ) )
  )
  */

//  override def settings = Project.defaultSettings ++ buildSettings
  lazy val slickKey = TaskKey[Seq[File]]("gen-tables")
  lazy val slickCodeGenTask = (
    sourceManaged,
    dependencyClasspath in Compile,
    runner in Compile,
    streams,
    dbDriver,
    dbUrl,
    dbLogin,
    dbPasswd,
    dbSlickDriver,
    slickGenPackage
    ) map { (dir, cp, r, s,jdbcDriver,url,login,passwd,slickDriver,pkg) =>

      val outputDir = (dir / "slick").getPath // place generated files in sbt's managed sources folder

      toError(r.run("scala.slick.codegen.SourceCodeGenerator", cp.files, Array(slickDriver, jdbcDriver, url, outputDir, pkg, login, passwd), s.log))
      val fname = outputDir + "/slickfun/example03/gen/Tables.scala"
    Seq(file(fname))
  }
}

