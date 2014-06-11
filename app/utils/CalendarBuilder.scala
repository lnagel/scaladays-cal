package utils

import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.property._

object CalendarBuilder {
  def createCalendar = {
    val calendar = new Calendar()

    calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"))
    calendar.getProperties().add(Version.VERSION_2_0)
    calendar.getProperties().add(CalScale.GREGORIAN)
    
    calendar
  }
  
//  def createCalendar(sessions: List[Session]) = {
//    
//  }

}