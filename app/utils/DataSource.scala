package utils

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws._
import play.api.Play.current
import models._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.concurrent.Future

object DataSource {
  
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  
  val timePeriodReads = __.read[String].map { v =>
    val fmt = DateTimeFormat.forPattern("HH:mm")
    v.split("-") match {
      case Array(start, end) => {
        Some((fmt.parseDateTime(start), fmt.parseDateTime(end)))
      }
      case _ => None
    }
  }
  
  implicit val sessionReads = (
      (JsPath \ "title").read[String] and 
      (JsPath \ "time").read(timePeriodReads)
    )(Session.apply _)
  
  val dayDateReads = __.read[String].map { v =>
    val day = Integer.parseInt(v.replaceAll("[^0-9]", ""))
    new DateTime(2014, 6, day, 0, 0)
  }
  
  implicit val dayReads = (
      (JsPath \ "title").read(dayDateReads) and 
      (JsPath \ "tracks").read[Seq[Session]]
    )(Day.apply _)

  implicit val dataReads = (
      (JsPath \ "Day1").read[Day] and
      (JsPath \ "Day2").read[Day] and
      (JsPath \ "Day3").read[Day]
    ).apply((day1, day2, day3) => Data(Seq(day1, day2, day3)))

  def makeRequest = {
    WS.url("http://scaladays.org/data.json").get()
  }
  
  def requestData = {
    makeRequest.map { response =>
      response.json.validate[Data]
    }
  }
  
  def data: Future[Option[Seq[Session]]] = {
    requestData.map { jsResult =>
      jsResult match {
        case s: JsSuccess[Data] => {
          val sessions = s.get.days.flatMap { d =>
            d.sessions.map { s =>
              Session(s.title, Session.correctTimes(d, s))
            }
          }
          
          Some(sessions)
        }
        case e: JsError => {
          None
        }
      }
    }
  }
}