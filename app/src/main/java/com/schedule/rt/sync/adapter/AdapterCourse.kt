package com.schedule.rt.sync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassCourse

class AdapterCourse: RecyclerView.Adapter<AdapterCourse.ViewHolder>() {

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

        holder.btnSecond.visibility = View.INVISIBLE

        holder.btnFirst.text = "Add"
        holder.btnFirst.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.add, 0, 0, 0)
        holder.btnFirst.setOnClickListener {
            clickListener.onAddClick(position)
        }
    }

    override fun getItemCount(): Int {
        return dataClassCourse.size
    }

    class ViewHolder(itemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val tvTittle: TextView = itemView.findViewById(R.id.tvTittle)
        val tvData1: TextView = itemView.findViewById(R.id.tvData1)
        val btnFirst: Button = itemView.findViewById(R.id.btnFirst)
        val btnSecond: Button = itemView.findViewById(R.id.btnSecond)
    }

    fun updateRvCourse(dataClassCourse: List<DataClassCourse>) {
        this.dataClassCourse.clear()
        this.dataClassCourse.addAll(dataClassCourse)
        notifyDataSetChanged()
    }

    interface onItemClickListener {
        fun onAddClick(position: Int)
    }

    fun setOnItemClickListener(onClickListener: onItemClickListener) {
        clickListener = onClickListener
    }
}