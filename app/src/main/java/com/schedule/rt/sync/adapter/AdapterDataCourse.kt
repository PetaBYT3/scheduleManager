package com.schedule.rt.sync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassCourse
import com.schedule.rt.sync.viewmodel.ViewModelLecturer

class AdapterDataCourse(
    private val vmLecturer: ViewModelLecturer,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<AdapterDataCourse.ViewHolder>() {

    val dataClassCourse = mutableListOf<DataClassCourse>()

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
        holder.tvData1.text = buildString {
            append(currentItem.sksCourse)
            append(" SKS")
        }

        holder.tvData2.visibility = View.VISIBLE
        vmLecturer.getLecturerByUid(currentItem.uidLecturer).observe(lifecycleOwner) {
            holder.tvData2.text = buildString {
                append(it?.nameLecturer)
            }
        }

        holder.btnEdit.setOnClickListener {
            clickListener.onEditClick(position)
        }

        holder.btnDelete.setOnClickListener {
            clickListener.onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int {
        return dataClassCourse.size
    }

    class ViewHolder(itemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val tvTittle: TextView = itemView.findViewById(R.id.tvTittle)
        val tvData1: TextView = itemView.findViewById(R.id.tvData1)
        val tvData2: TextView = itemView.findViewById(R.id.tvData2)
        val btnEdit: Button = itemView.findViewById(R.id.btnFirst)
        val btnDelete: Button = itemView.findViewById(R.id.btnSecond)
    }

    fun updateRvCourse(newDataClassCourse: List<DataClassCourse>) {
        dataClassCourse.clear()
        dataClassCourse.addAll(newDataClassCourse)
        notifyDataSetChanged()
    }

    interface onItemClickListener {
        fun onEditClick(position: Int)
        fun onDeleteClick(position: Int)
    }

    fun setOnItemClickListener(onClickListener: onItemClickListener) {
        clickListener = onClickListener
    }
}