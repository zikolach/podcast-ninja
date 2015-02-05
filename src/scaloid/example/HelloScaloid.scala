package scaloid.example

import android.app.ExpandableListActivity
import org.scaloid.common._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class HelloScaloid extends ExpandableListActivity with SActivity {

  onCreate {
    val rssAdapter = new PodcastListAdapter()
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
}


