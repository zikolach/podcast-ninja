package scaloid.example

import java.util.Date

case class Episode(name: String,
                   description: Option[String] = None,
                   url: String,
                   date: Date = new Date()) {
  override def toString = name
}