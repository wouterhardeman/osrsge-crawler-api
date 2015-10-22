package controllers

import org.h2.jdbc.JdbcSQLException
import org.htmlcleaner.HtmlCleaner
import play.api.db.DB
import play.api.libs.json.Json
import play.api.mvc._
import scala.collection.mutable
import scala.io.Source
import scala.collection.immutable.{HashMap, Map}
import play.api.Play.current

/**
 * Created by Wouter on 10/21/2015.
 */
object Crawler extends Controller {

  case class Item(id: Int, name:String, price:String)
  val dbName = "TEST"

  implicit val itemsWrites = Json.writes[Item]

  def index = Action {
    val list = 10346 to 10347
    val string = list.foldLeft(1)((c, e) => c * e)
    //val strings = itemToSQL(generateItemFromPage(835))
    //insertDb(getItems(list))
    Ok("Test")
  }

  def selectDb = Action {
    val conn = DB.getConnection().createStatement()
    try {
      var items = mutable.MutableList[Item]()
      val rs = conn.executeQuery("SELECT * FROM " + dbName)
      while(rs.next()) {
        items += new Item(rs.getInt("ID"), rs.getString("NAME"), rs.getString("PRICE"))
      }
      Ok(Json.toJson(items))
    } catch {
      case ex: JdbcSQLException => {
        Ok("Error while excuting the query: " + ex.getMessage)
      }
    } finally {
      conn.close()
    }
  }

  def itemToSQL(item : Item) = if(item != null) "(" + item.id + ", '" + item.name + "', '" + item.price + "'), " else ""

  def insertDb() = Action {
    val itemList = getItems(10346 to 10347)
    val sql = itemList.foldLeft("INSERT INTO " + dbName + " VALUES ")( (c, e) =>
      c + itemToSQL(e._2)).dropRight(2)
    val conn = DB.getConnection().createStatement()
    try {
      conn.execute(sql)
      Ok("Info inserted!")
    } catch {
      case ex: JdbcSQLException => {
        Ok("Error while inserting into the database: " + ex.getMessage)
      }
    } finally {
      conn.close()
    }
  }

  def createDb = Action {
    val conn = DB.getConnection().createStatement()
    try {
      conn.execute("CREATE TABLE " + dbName + "(ID INT PRIMARY KEY, NAME VARCHAR(255), PRICE VARCHAR(255))")
      Ok("Database created!")
    } catch {
      case ex: JdbcSQLException => {
        Ok("Error while creating the database: " + ex.getMessage)
      }
    } finally {
      conn.close()
    }
  }

  def deleteDb = Action {
    val conn = DB.getConnection().createStatement()
    try {
      conn.execute("DROP TABLE " + dbName)
      Ok("Database deleted!")
    } catch {
      case ex: JdbcSQLException => {
        Ok("Error while deleting the database: " + ex.getMessage)
      }
    } finally {
      conn.close()
    }
  }

  def getItems(range : Range) = range.foldLeft(new HashMap[Int, Item]) ((c, e) =>
    c.updated(e, generateItemFromPage(e))
  )

  def generateItemFromPage(id : Int) : Item = {
    val source = getItemPage(id)
    val cleaner = new HtmlCleaner
    val clean = cleaner.clean(source)
    try {
      val name = clean.getElementsByName("h2", true)(0).getText.toString
      //val price2 = clean.getElementsByName("h3", true)(0).getElementsByName("span", true)(0).getText.toString.replaceAll("[^0-9]+", "")
      val price = clean.getElementsByName("h3", true)(0).getElementsByName("span", true)(0).getAttributeByName("title")
      new Item(id, name, price)
    } catch {
      case ex: ArrayIndexOutOfBoundsException => {
        return null
      }
    }
  }

  def getItemPage(id : Int) =
    Source.fromURL("http://services.runescape.com/m=itemdb_oldschool/Lockpick/viewitem?obj=" + id).mkString

}
