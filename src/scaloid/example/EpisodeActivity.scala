package scaloid.example

import java.text.SimpleDateFormat
import java.util.Locale

import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Html.ImageGetter
import android.view.Gravity
import org.scaloid.common.{SActivity, STextView, SVerticalLayout}
import org.scaloid.common._

class EpisodeActivity extends SActivity {

  private val dateFormatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH)

  onCreate {

    val episode = getIntent.getSerializableExtra("episode").asInstanceOf[Episode]
    val description = Html.fromHtml(episode.description.getOrElse(""), new ImageGetter {
      override def getDrawable(source: String): Drawable = {
        val d = getResources.getDrawable(R.drawable.ic_img)
        d.setBounds(0, 0, d.getIntrinsicWidth, d.getIntrinsicHeight)
        d
      }
    }, null)

    contentView = new SVerticalLayout {
      setTitle(episode.name)
      info(Html.fromHtml(episode.description.getOrElse("")).toString)
      STextView(episode.name).gravity(Gravity.CENTER).height(20.dip)
      STextView(dateFormatter.format(episode.date)).gravity(Gravity.CENTER).height(20.dip)
      STextView(description).fill.gravity(Gravity.FILL)
    } padding 10.dip
  }

}
