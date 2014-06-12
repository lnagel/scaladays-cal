package controllers

import play.api.Play.current
import play.api.cache.Cached
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.RequestHeader
import utils.CalendarBuilder
import utils.DataSource

object Application extends Controller {
  
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def index = Action {
    Ok("")
  }
  
  def calendar = Cached((_ => "sessions.ical"): (RequestHeader => String), 120) {
    Action.async {
      DataSource.sessionList map {
        sessions => Ok(CalendarBuilder.createCalendar(sessions).toString)
      } recover {
        case _ => InternalServerError("No valid data received")
      }
    }
  }

}