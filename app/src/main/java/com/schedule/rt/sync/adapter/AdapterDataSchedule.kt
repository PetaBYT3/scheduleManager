package com.schedule.rt.sync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassCourse
import com.schedule.rt.sync.function.dpToPx
import com.schedule.rt.sync.viewmodel.ViewModelLecturer

class AdapterDataSchedule(
    private val vmLecturer: ViewModelLecturer,
    private val lifecycleOwner: LifecycleOwner
): RecyclerView.Adapter<AdapterDataSchedule.ViewHolder>() {

    var dataClassCourse = mutableListOf<DataClassCourse>()

    private lateinit var clickListener: onItemClickListener

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_parent, parent, false)
        return ViewHolder(itemView, clickListener)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val currentItem = dataClassCourse[position]
        holder.tvTittle.text = currentItem.nameCourse
        holder.tvData1.text = currentItem.sksCourse

        holder.tvData2.visibility = View.VISIBLE
        vmLecturer.getLecturerByUid(currentItem.uidLecturer).observe(lifecycleOwner) {
            holder.tvData2.text = it?.nameLecturer
        }

        holder.tvData3.visibility = View.VISIBLE
        holder.tvData3.text = buildString {
            append(currentItem.startTime)
            append(" Until ")
            append(currentItem.endTime)
        }


        holder.btnFirst.text = "Delete"
        holder.btnFirst.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.delete, 0, 0, 0)
        holder.btnFirst.setOnClickListener {
            clickListener.onDeleteClick(position)
        }

        holder.btnSecond.visibility = View.INVISIBLE

        //Add 10dp For First Item
        val layoutParams = holder.itemView.layoutParams as MarginLayoutParams
        layoutParams.topMargin = if (position == 0) dpToPx(10) else 0
        holder.itemView.layoutParams = layoutParams
    }

    override fun getItemCount(): Int {
        return dataClassCourse.size
    }

    class ViewHolder(itemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val tvTittle: TextView = itemView.findViewById(R.id.tvTittle)
        val tvData1: TextView = itemView.findViewById(R.id.tvData1)
        val tvData2: TextView = itemView.findViewById(R.id.tvData2)
        val tvData3: TextView = itemView.findViewById(R.id.tvData3)
        val btnFirst: TextView = itemView.findViewById(R.id.btnFirst)
        val btnSecond: TextView = itemView.findViewById(R.id.btnSecond)
    }

    fun updateRvSchedule(newDataClassCourse: List<DataClassCourse>) {
        dataClassCourse.clear()
        dataClassCourse.addAll(newDataClassCourse)
        notifyDataSetChanged()
    }

    interface onItemClickListener {
        fun onDeleteClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        clickListener = listener
    }
}