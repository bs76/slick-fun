package slickfun.example02

import scala.slick.driver.H2Driver.simple._

object Main extends App {
  import DB._
  import util.Try 
  

  db withSession{ implicit sess => 
    //drop schema if exists
    Try( (users.ddl ++ projects.ddl ++ userProjects.ddl).drop )
    (users.ddl ++ projects.ddl ++ userProjects.ddl).create

    users ++= Seq(
      (None,"Homer","Simpson",false,38,Some("grease salesman")),
      (None,"Jimbo","Jones",true,13,None),
      (None,"Moe","Szyszlak",false,47,Some("Barman")),
      (None,"Patty","Bouvier",false,45,None)
    )

    projects ++= Seq(
      (None,"Home Duff brewery"),
      (None,"Lizard farm"),
      (None,"Skinner pranking")
    )


    val userForProject:(String,String) => Seq[(Int,Int)]  = (uname,projLike) => (
      for{
        uid <- users.filter(_.firstName === uname).map(_.id).firstOption
        pid <- projects.filter(_.projName like projLike).map(_.id).firstOption
      }
      yield (uid,pid)
    ).toSeq

    userProjects ++= userForProject("Homer","%Duff%")
    userProjects ++= userForProject("Homer","%Skinner%")
    userProjects ++= userForProject("Moe","%Duff%")
    userProjects ++= userForProject("Moe","%Lizard%")
    userProjects ++= userForProject("Moe","%Skinner%")

    // we use foreign keys to fetch referenced  rows
    val allUserProjects = for{ 
      up <- userProjects
      u <- up.user
      p <- up.project 
    }
      yield (u,p)


    allUserProjects.list.foreach(println)

    //only Homer's projects -- (filter joined table)
    val homersProjects  = for{ 
      (up,u) <- userProjects leftJoin users on (_.uid === _.id) if u.firstName === "Homer"
      p <- up.project
    }
      yield (u.id,p)

    println("Only homers projects:")
    homersProjects.list.foreach(println)

    // find other users who have a project in common with Homer
    // we join homersProjects (above) (Whoaaaa!)
    val commonWithHomer = for {
      (up,(id,p)) <- userProjects leftJoin homersProjects on (_.pid === _._2.id) if up.uid =!= id
      p <- up.project
      u <- up.user
    }
      yield (u,p)

    commonWithHomer.foreach(println)
  }
}

object DB {
  def db = Database.forURL("jdbc:h2:./target/example02", driver = "org.h2.Driver") 

  val users = TableQuery[Users]
  val projects = TableQuery[Projects]
  val userProjects = TableQuery[UsersProjects]

  class UsersProjects(tag:Tag) extends Table[(Int,Int)](tag,"USER_PROJECTS"){
    def pid = column[Int]("PID" )
    def uid = column[Int]("UID" )

    def * = (uid, pid)
    def pk = primaryKey("pk_a", (uid,pid))

    def user = foreignKey("USR_FK", uid , users)(_.id)
    def project = foreignKey("PRJ_FK", pid , projects)(_.id)
  }

  class Users(tag: Tag) extends Table[(Option[Int], String, String, Boolean, Int,Option[String])](tag, "USERS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def firstName = column[String]("FIRST_NAME", O.DBType("varchar(40)"))
    def lastName = column[String]("LASTNAME")
    def hipster = column[Boolean]("HIPSTER")
    def job = column[Option[String]]("JOB")
    def age = column[Int]("AGE")
    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id.?, firstName, lastName , hipster, age, job)
  }
  class Projects(tag: Tag) extends Table[(Option[Int], String)](tag, "PROJECTS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def projName = column[String]("PROJECT_NAME", O.DBType("varchar(40)"))
    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id.?, projName)
  }

}
