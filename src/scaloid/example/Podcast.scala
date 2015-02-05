package scaloid.example

import java.net.URL
import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import scala.concurrent.{Promise, Future}
import scala.util.{Failure, Success, Try}
import scala.xml.{XML, NodeSeq}
import scala.concurrent.ExecutionContext.Implicits.global

case class Podcast(url: String, name: Option[String] = None) {
  var episodes: Seq[Episode] = Nil

  private val dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)

  private def parseAtomDate(date:String, formatter:SimpleDateFormat):Date = {
    val newDate = date.reverse.replaceFirst(":", "").reverse
    formatter.parse(newDate)
  }

  private def getHtmlLink(node: NodeSeq) =
    node.filter(n => (n \ "@type").text == "text/html").map( n => (n \ "@href").text).head


  def fetchEpisodes: Future[Unit] = {
    val p = Promise[Unit]()
    Future {
      Try(new URL(url).openConnection().getInputStream) match {
        case Success(stream) =>
          val xml = XML.load(stream)
          if((xml \\ "channel").length == 0) {
            // atom
            for (feed <- xml \\ "feed") yield {
              episodes = for (item <- feed \\ "entry") yield Episode(
                name = (item \\ "title").text,
                url = getHtmlLink(item \\ "link"),
                description = Some((item \\ "summary").text),
                date = parseAtomDate((item \\ "published").text, dateFormatter)
                //guid = (item \\ "id").text
              )
            }
          } else {
            //rss
            for (channel <- xml \\ "channel") yield {
              episodes = for (item <- channel \\ "item") yield Episode(
                name = (item \\ "title").text,
                url = (item \\ "link").text,
                description = Some((item \\ "description").text),
                date = dateFormatter.parse((item \\ "pubDate").text)
                //guid = (item \\ "guid").text
              )
            }
          }
          p.success()
        case Failure(e) =>
          p.failure(e)
      }
    }
    p.future
  }

  override def toString = name.getOrElse(url)

}