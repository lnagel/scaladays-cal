package utils

import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.property._
import models.Session
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.TimeZoneRegistryFactory
import net.fortuna.ical4j.model.component.VTimeZone
import net.fortuna.ical4j.model.property._

import org.joda.time.{DateTime => JodaDateTime}
import net.fortuna.ical4j.model.{DateTime => IcalDateTime}

object CalendarBuilder {
  
  implicit def JodaDateTime2IcalDateTime(time: JodaDateTime): IcalDateTime = {
    new IcalDateTime(time.getMillis())
  }
  
  implicit val timezone = {
    val registry = TimeZoneRegistryFactory.getInstance().createRegistry()
    val timezone = registry.getTimeZone("Europe/Berlin")
    timezone.getVTimeZone()
  }
  
  def initCalendar = {
    val calendar = new Calendar()

    calendar.getProperties().add(Version.VERSION_2_0)
    calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"))
    calendar.getProperties().add(CalScale.GREGORIAN)
    
    calendar.getProperties().add(new XProperty("X-WR-CALNAME", "Scala Days 2014"))
    calendar.getProperties().add(new XProperty("X-WR-TIMEZONE", "Europe/Berlin"))
    
    calendar
  }
  
  def initEvent(session: Session)(implicit tz: VTimeZone): Option[VEvent] = {
    session.times match {
      case Some((start, end)) => {
        val meeting = new VEvent(start, end, session.title)
        meeting.getProperties().add(tz.getTimeZoneId)
        
        val default = "Kosmos Berlin, Karl-Marx-Allee 131a, Berlin"
        val location = session.location.map(room => s"Room $room, $default").getOrElse(default) 
        meeting.getProperties().add(new Location(location))
        
        val speakerDetails = session.speakers match {
          case Some(speakers) => speakers.map { speaker => 
            List(Some(speaker.name), speaker.company, speaker.twitter).flatten.filter(_.trim.nonEmpty).mkString(", ")
          }
          case None => Seq()
        }
        
        val details = (session.details, speakerDetails) match {
          case (Some(sessionDetails), Nil) => sessionDetails
          case (None, speakers) => speakers.mkString("\n")
          case (Some(sessionDetails), speakers) => speakers.mkString("\n") + "\n\n" + sessionDetails
          case _ => ""
        }
        
        meeting.getProperties().add(new Description(details))
        
        Some(meeting)
      }
      case None => None
    }
  }
  
  def createCalendar(sessions: Seq[Session]) = {
    val calendar = initCalendar
    val events = sessions.map(initEvent).flatten
    
    events.foreach(calendar.getComponents().add(_))
    
    calendar
  }

}