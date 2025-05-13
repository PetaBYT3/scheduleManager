package com.schedule.rt.sync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassCourse

class AdapterCheckSchedule: RecyclerView.Adapter<AdapterCheckSchedule.ViewHolder>() {

    var dataClassCourse = mutableListOf<DataClassCourse>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_time, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val currentItem = dataClassCourse[position]
        holder.tvTime.text = buildString {
            append(currentItem.startTime)
            append(" - ")
            append(currentItem.endTime)
        }
    }

    override fun getItemCount(): Int {
        return dataClassCourse.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    fun updateData(newData: List<DataClassCourse>) {
        dataClassCourse.clear()
        dataClassCourse.addAll(newData)
        notifyDataSetChanged()
    }
}