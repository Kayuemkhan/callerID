package com.chromatics.caller_id.ui.incoming_calls

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chromatics.caller_id.R
import com.chromatics.caller_id.common.CallLogItem
import com.chromatics.caller_id.common.CallLogItemGroup
import com.chromatics.caller_id.common.NumberInfo
import com.chromatics.caller_id.utils.IconAndColor
import com.chromatics.caller_id.utils.NumberInfoUtils

class CallLogItemRecyclerViewAdapter :
    PagingDataAdapter<CallLogItemGroup, CallLogItemRecyclerViewAdapter.ViewHolder>(
        DiffUtilCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.call_log_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group)
    }

    private fun getLabel(context: Context, item: CallLogItem): String {
        return item.number // Replace with logic to get the label from CallLogItem
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val callTypeIcons = listOf(
            view.findViewById<AppCompatImageView>(R.id.callTypeIcon),
            view.findViewById(R.id.callTypeIcon2),
            view.findViewById(R.id.callTypeIcon3)
        )
        private val label: TextView = view.findViewById(R.id.item_label)
        private val numberInfoIcon: AppCompatImageView = view.findViewById(R.id.numberInfoIcon)
        private val duration: TextView = view.findViewById(R.id.duration)
        private val description: TextView = view.findViewById(R.id.description)
        private val time: TextView = view.findViewById(R.id.time)

        fun bind(group: CallLogItemGroup?) {
            if (group == null) {
                setPlaceholder()
                return
            }

            val context = itemView.context
            val firstItem = group.items.firstOrNull()
            if (firstItem == null) {
                setPlaceholder()
                return
            }

            label.text = getLabel(context, firstItem)
            label.visibility = View.VISIBLE

            bindNumberInfoIcon(firstItem.numberInfo)
            bindDuration(context, firstItem)
            bindTypeIcons(group)
            bindDescription(context, firstItem.numberInfo)
            bindTime(context, group)
        }

        private fun setPlaceholder() {
            listOf(label, time).forEach { it.visibility = View.INVISIBLE }
            listOf(duration, description).forEach { it.visibility = View.GONE }
            callTypeIcons.forEach { it.visibility = View.GONE }
            numberInfoIcon.visibility = View.INVISIBLE
        }

        private fun bindNumberInfoIcon(numberInfo: NumberInfo) {
            val iconAndColor = IconAndColor.forNumberRating(
                numberInfo.rating,
                numberInfo.contactItem != null
            )
            if (iconAndColor.noInfo) {
                numberInfoIcon.setImageDrawable(null)
                numberInfoIcon.visibility = View.INVISIBLE
            } else {
                iconAndColor.applyToImageView(numberInfoIcon)
                numberInfoIcon.visibility = View.VISIBLE
            }
        }

        private fun bindDuration(context: Context, item: CallLogItem) {
            if (item.duration == 0L &&
                (item.type == CallLogItem.Type.MISSED || item.type == CallLogItem.Type.REJECTED)
            ) {
                duration.visibility = View.GONE
            } else {
                duration.text = getDurationText(context, item.duration)
                duration.visibility = View.VISIBLE
            }
        }

        private fun bindTypeIcons(group: CallLogItemGroup) {
            group.items.forEachIndexed { index, item ->
                if (index < callTypeIcons.size) {
                    callTypeIcons[index].apply {
                        visibility = View.VISIBLE
                        getTypeIcon(item.type)?.let { setImageResource(it) }
                    }
                }
            }
            callTypeIcons.drop(group.items.size).forEach {
                it.visibility = View.GONE
            }
        }

        private fun bindDescription(context: Context, numberInfo: NumberInfo) {
            val descriptionText = NumberInfoUtils.getShortDescription(context, numberInfo)
            if (descriptionText.isNullOrEmpty()) {
                description.visibility = View.GONE
            } else {
                description.text = descriptionText
                description.visibility = View.VISIBLE
            }
        }

        private fun bindTime(context: Context, group: CallLogItemGroup) {
            val firstItem = group.items.first()
            var timeText = DateUtils.getRelativeTimeSpanString(
                firstItem.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_ABBREV_ALL
            ).toString()

            if (group.items.size > 3) {
                timeText = "(${group.items.size}) $timeText"
            }
            time.text = timeText
            time.visibility = View.VISIBLE
        }

        private fun getTypeIcon(type: CallLogItem.Type?): Int? {
            return when (type) {
                CallLogItem.Type.INCOMING -> R.drawable.ic_call_received_24dp
                CallLogItem.Type.OUTGOING -> R.drawable.ic_call_made_24dp
                CallLogItem.Type.MISSED -> R.drawable.ic_call_missed_24dp
                CallLogItem.Type.REJECTED -> R.drawable.ic_call_rejected_24dp
                else -> null
            }
        }

        private fun getDurationText(context: Context, duration: Long): String {
            val hours = duration / 3600
            val minutes = (duration % 3600) / 60
            val seconds = duration % 60

            return when {
                hours > 0 -> context.getString(R.string.duration_h_m_s, hours, minutes, seconds)
                minutes > 0 -> context.getString(R.string.duration_m_s, minutes, seconds)
                else -> context.getString(R.string.duration_s, seconds)
            }
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<CallLogItemGroup>() {
        override fun areItemsTheSame(oldItem: CallLogItemGroup, newItem: CallLogItemGroup): Boolean {
            return oldItem.items == newItem.items
        }

        override fun areContentsTheSame(oldItem: CallLogItemGroup, newItem: CallLogItemGroup): Boolean {
            return false
        }
    }
}
