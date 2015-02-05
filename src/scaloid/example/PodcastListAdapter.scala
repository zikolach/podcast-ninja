package scaloid.example

import android.view.{View, Gravity, ViewGroup}
import android.widget.{AbsListView, BaseExpandableListAdapter}
import org.scaloid.common.STextView

class PodcastListAdapter(implicit val context: android.content.Context) extends BaseExpandableListAdapter {

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