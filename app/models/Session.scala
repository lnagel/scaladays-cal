package models

import org.joda.time.DateTime

object Session {
  def withCorrectedTimes(day: Day, session: Session) = {
    Session(session.title, correctTimes(day, session), session.location, session.details, session.speakers)
  }
  
  def correctTimes(day: Day, session: Session) = {
    session.times match {
      case Some((start, end)) => {
        Some((combine(day.date, start), combine(day.date, end)))
      }
      case None => None
    }
  }
  
  def combine(date: DateTime, time: DateTime): DateTime = {
    new DateTime(
        date.getYear,
        date.getMonthOfYear,
        date.getDayOfMonth,
        time.getHourOfDay,
        time.getMinuteOfHour,
        time.getSecondOfMinute,
        time.getZone)
  }
}

case class Session(title: String, times: Option[Tuple2[DateTime,DateTime]], location: Option[Int], details: Option[String], speakers: Option[Seq[Speaker]])
