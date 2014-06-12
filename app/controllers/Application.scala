package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.ws._
import scala.concurrent.Future
import utils.DataSource
import models.Data
import org.joda.time._
import models.Session
import utils.CalendarBuilder
import play.api.cache._

object Application extends Controller {
  
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def index = Action {
    Ok("")
  }
  
  def calendar = Cached((_ => "sessions.ical"): (RequestHeader => String), 120) {
    Action.async {
      DataSource.data.map { sessionsOption =>
        sessionsOption match {
          case Some(sessions) => {
            val calendar = CalendarBuilder.createCalendar(sessions)
            Ok(calendar.toString)
          }
          case None => {
            InternalServerError("No valid data received")
          }
        }
      }
    }
  }

}