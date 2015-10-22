package controllers

import controllers.Crawler._
import org.h2.jdbc.JdbcSQLException
import play.api.db.DB
import play.api.libs.json.Json
import play.api.mvc._
import play.api.Play.current

import scala.collection.mutable

/**
 * Created by Wouter on 10/22/2015.
 */
object RestApi extends Controller {

  val dbName = "TEST"


  def allItems = Action {
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
        Ok("Error while executing the query: " + ex.getMessage)
      }
    } finally {
      conn.close()
    }
  }

  def item(id : Int) = Action {
    val conn = DB.getConnection().createStatement()
    try {
      val rs = conn.executeQuery("SELECT * FROM " + dbName + " WHERE id=" + id)
      if(rs.next()) {
        val item = new Item(rs.getInt("ID"), rs.getString("NAME"), rs.getString("PRICE"))
        Ok(Json.toJson(item))
      } else {
        Ok("Item not found.")
      }
    } catch {
      case ex: JdbcSQLException => {
        Ok("Error while executing the query: " + ex.getMessage)
      }
    } finally {
      conn.close()
    }
  }

}
