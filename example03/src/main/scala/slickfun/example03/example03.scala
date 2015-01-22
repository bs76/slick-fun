package slickfun.example03


object Main extends App {

  //Look Ma, no database !
  // we generated Tables with slick generator
  // generated class: slickfun.example03.gen (under target/scala-2.11/src-manageed/slick
  // see also project/Build.scala in top project and also build.sbt (DB connection)

  import slickfun.example03.gen.Tables._

  //imports profile *and* Database for us
  import profile.simple._
  import scala.slick.jdbc.{GetResult, StaticQuery => Q}
  import Q.interpolation

  def db = Database.forURL("jdbc:sqlite:./db/example03_gen.db", driver = "org.sqlite.JDBC")

  db.withSession{ implicit sess =>
    // note tha SQLite's boolean is actually and int :-/

    println("We got these users in DB:")
    Users.foreach(println)

    //this is our tree
    Tree.foreach(println)

    //we *WANT* to run recursive select to find the path from node with id X upwards the tree
    def treePath(fromId:Int) = sql"""
        WITH RECURSIVE tree1(id,parent,label) AS (
            SELECT t.id, t.parent,t.label from tree t where t.id=$fromId
            UNION ALL
            SELECT tree.id,tree.parent,tree.label FROM tree,tree1 WHERE tree1.parent=tree.id)
        SELECT * FROM tree1;
      """.as[TreeRow]
      //also can use:
      //""".as[(Option[Int],Option[Int],Option[String])]

    println("Path to node: 3")
    treePath(3).foreach(println)

    println("Path to node: 6")
    treePath(6).foreach(println)



  }
}

