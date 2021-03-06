package utils

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws._
import play.api.Play.current
import models._
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormat
import scala.concurrent.Future
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object DataSource {
  
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  
  val cleanedStringReads = __.read[String].map { v =>
    val doc: Document = Jsoup.parseBodyFragment(v)
    doc.body.text
  }
  
  def parseTime(time: String) = {
    val tz = DateTimeZone.forID("Europe/Berlin")
    val fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").withZone(tz)
    fmt.parseDateTime(s"2014-06-01 $time")
  }
  
  val timePeriodReads = __.read[String].map { v =>
    v.split("-") match {
      case Array(start, end) => {
        Some((parseTime(start), parseTime(end)))
      }
      case _ => None
    }
  }
  
  implicit val speakerReads = (
      (JsPath \ "fullname").read(cleanedStringReads) and 
      (JsPath \ "company").readNullable(cleanedStringReads) and 
      (JsPath \ "twitter").readNullable(cleanedStringReads)
    )(Speaker.apply _)
  
  implicit val sessionReads = (
      (JsPath \ "title").read(cleanedStringReads) and 
      (JsPath \ "time").read(timePeriodReads) and 
      (JsPath \ "room").readNullable[Int] and 
      (JsPath \ "description").readNullable(cleanedStringReads) and
      (JsPath \ "speakers").readNullable[Seq[Speaker]]
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
  
  def wsRequest: Future[WSResponse] = {
    WS.url("http://scaladays.org/data.json").get()
  }
  
  def jsonResponse: Future[JsResult[Data]] = {
    wsRequest.map { response =>
      response.json.validate[Data]
    }
  }
  
  def sessionList: Future[Seq[Session]] = {
    jsonResponse.map {
      case success: JsSuccess[Data] => {
        success.get.days.flatMap { d =>
          d.sessions.map { s =>
            Session.withCorrectedTimes(d, s)
          }
        }
      }
      case error: JsError => {
        throw new RuntimeException(error.toString)
      }
    }
  }
}