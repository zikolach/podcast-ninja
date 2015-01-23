package scaloid.example

import java.net.URL
import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import android.app.ExpandableListActivity
import android.text.Html
import android.view._
import android.widget.{AbsListView, BaseExpandableListAdapter}
import org.scaloid.common._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}
import scala.xml.{NodeSeq, XML}


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

case class Episode(name: String,
                   description: Option[String] = None,
                   url: String,
                   date: Date = new Date()) {
  override def toString = name
}

class HelloScaloid extends ExpandableListActivity with SActivity {

  onCreate {
    val rssAdapter = new RSSLIstAdapter()
    setListAdapter(rssAdapter)
    getExpandableListView.onChildClick {
      (_, _, group, child, _) =>
        val episode = rssAdapter.getChild(group, child).asInstanceOf[Episode]
        SIntent[Episode].put(episode).start[EpisodeActivity]
        true
    }
    getExpandableListView.onGroupClick {
      (_, _, group, _) =>
        val podcast = rssAdapter.getGroup(group).asInstanceOf[Podcast]
        if (podcast.episodes.isEmpty) {
          val progress = spinnerDialog(podcast.toString, "Fetching episodes...")
          podcast.fetchEpisodes.onComplete {
            case Success(_) =>
              toast(s"${podcast.episodes.size} episodes fetched")
              progress.dismiss()
            case Failure(e) =>
              toast(e.getMessage)
              progress.dismiss()
          }
        }
        podcast.episodes.isEmpty
    }
  }

  class RSSLIstAdapter extends BaseExpandableListAdapter {

    private val podcasts: Array[Podcast] = Array(Podcast(url = "http://radius.podfm.ru/rss/rss.xml"), Podcast(url = "http://mds.podfm.ru/rss/rss.xml"))

    def getGenericView = new STextView {
      layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 64)
      gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT
      setPadding(36, 0, 0, 0)
    }

    def isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    def hasStableIds: Boolean = true

    def getGroupCount: Int = podcasts.size

    def getChildrenCount(groupPosition: Int): Int = podcasts(groupPosition).episodes.size

    def getGroupId(groupPosition: Int): Long = groupPosition

    def getChildId(groupPosition: Int, childPosition: Int): Long = childPosition

    def getChild(groupPosition: Int, childPosition: Int): AnyRef = podcasts(groupPosition).episodes(childPosition)

    def getGroup(groupPosition: Int): AnyRef = podcasts(groupPosition)

    def getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View, parent: ViewGroup): View =
      getGenericView.text = getGroup(groupPosition).toString

    def getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View, parent: ViewGroup): View =
      getGenericView.text = getChild(groupPosition, childPosition).toString
  }

}


