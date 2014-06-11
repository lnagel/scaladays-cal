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

object Application extends Controller {
  
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def dataJson = Action.async {
    DataSource.makeRequest.map { response =>
      Ok(response.body)
    }
  }
  
  def sessionsJson = Action.async {
    DataSource.data.map { sessionsOption =>
      sessionsOption match {
        case Some(sessions) => {
          val jsObjects = sessions.map { s =>
            JsObject(
                Seq(
                    "start_time" -> JsString(s.time.map(_._1.toString()).getOrElse("")),
                    "end_time" -> JsString(s.time.map(_._2.toString()).getOrElse("")),
                    "title" -> JsString(s.title)
                  )
              )
          }

          Ok(Json.prettyPrint(JsArray(jsObjects)))
        }
        case None => Ok(Json.prettyPrint(JsObject(Seq("error" -> JsString("true")))))
      }
    }
  }
  
  def sessionsIcal = Action.async {
    DataSource.data.map { sessionsOption =>
      sessionsOption match {
        case Some(sessions) => {
          val calendar = CalendarBuilder.createCalendar(sessions)

          Ok(calendar.toString)
        }
        case None => Ok("")
      }
    }
  }

}