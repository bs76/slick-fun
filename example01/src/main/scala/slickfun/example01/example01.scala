package slickfun.example01

import scala.slick.driver.H2Driver.simple._


object Main extends App {
  import DB._
  import util.Try

  val users = TableQuery[Users]

  db withSession{ implicit sess =>
    //drop schema if exists
    Try( users.ddl.drop )
    //ceate new one
    users.ddl.create

    // users.size.run is actually a query
    println(s"There are: #${users.size.run} users in the DB")

    //Add Homer
    users += (None,"Homer","Simpson",false,38,Some("grease salesman"))

    //filter out users with id 1, get first one as an option
//    val withIdOne = users.filter(_.id === 1).first
    val withIdOne = users.filter(_.id === 1).firstOption
    println("User with id 1:"+withIdOne)

    //all users over 30
    val over30 = users.filter(_.age >= 30)

    println(s"Select for over30: ${over30.selectStatement}")

    over30.run.foreach(println)

    println(s"There are: #${users.size.run} users in the DB")

    //Add some more users; although id is Int in class Users it's defined as Option[Int]
    //so no need to supply pk (as it's generated would not make much sense)
    users += (None,"Jimbo","Jones",true,13,None)
    users ++= Seq(
      (None,"Moe","Szyszlak",false,47,Some("Barman")),
      (None,"Patty","Bouvier",false,45,None)
    )

    //for comprehension for querying (will see more when doing joins)
    val q = for{ u <- users if u.age > 30 && u.age < 50 }
      //yield (u.firstName + u.lastName )   Won't work as u.firstName is Column[String]
      yield (u.firstName ++ LiteralColumn[String](" ") ++ u.lastName ) //this yield on column only

    val names:Seq[String]  = q.run
    names.foreach(println)

    println("How long is tour name ?")
    val dbLenFunc = SimpleFunction.unary[String,Int]("LENGTH")
    val userNameLen = for{ u <- users }
      yield (u.firstName, dbLenFunc(u.firstName ))

    userNameLen.foreach(println)

    println("How manu peeople with same length name ?")
    def nameCounts = userNameLen.groupBy(_._2).map{ case (count,len) => (count,len.size) }.list
    nameCounts.foreach(println)
    nameCounts.foreach{ case (count,len) => println(s"Length $len -> $count")}

    users += (None,"Nelson","Muntz",false,12,Some("Bully"))
    println("Names with Nelson:")
    nameCounts.foreach(println)
    users.filter(_.firstName === "Neslon").delete

    //this is how you
    //
    // get SQL statement:
    users.ddl.createStatements.foreach(println)

    //Update
    val moe = users.filter(_.firstName like "%Moe%");

    println("Moe -> Moe Joe")
    val u = moe.map(_.firstName).update(moe.map(_.firstName ++ LiteralColumn(" Joe")).first)
    println(s"Updated # rows: $u")
    users.foreach(println)

    println("Will delete %moe%")
    moe.delete
    users.foreach(println)

    println(s"Is Moe still there ? ${moe.list}")

  }



}

object DB {
  implicit val db = Database.forURL("jdbc:h2:./target/example01", driver = "org.h2.Driver")

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
}
