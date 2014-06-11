package models

import org.joda.time.DateTime

case class Day(date: DateTime, sessions: Seq[Session])
