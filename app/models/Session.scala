package models

import org.joda.time.DateTime

object Session {
  def withCorrectedTimes(day: Day, session: Session) = {
    Session(session.title, correctTimes(day, session), session.location, session.details)
  }
  
  def correctTimes(day: Day, session: Session) = {
    session.time match {
      case Some((start, end)) => {
        Some((combine(day.date, start), combine(day.date, end)))
      }
      case None => None
    }
  }
  
  def combine(date: DateTime, time: DateTime): DateTime = {
    date
      .withHourOfDay(time.getHourOfDay)
      .withMinuteOfHour(time.getMinuteOfHour)
      .withSecondOfMinute(time.getSecondOfMinute)
  }
}

case class Session(title: String, time: Option[Tuple2[DateTime,DateTime]], location: Option[Int], details: Option[String])
