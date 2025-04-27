package com.schedule.rt.sync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassLecturer

class AdapterDataManager: RecyclerView.Adapter<AdapterDataManager.ViewHolder>() {

    val dataClassLecturer = mutableListOf<DataClassLecturer>()
    private lateinit var listener: onItemClickListener

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_parent, parent, false)
        return ViewHolder(itemView, listener)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.btnSecond.visibility = View.INVISIBLE

        val currentItem = dataClassLecturer[position]
        holder.tvTittle.text = currentItem.nameLecturer
        holder.tvData1.text = currentItem.nikLecturer

        holder.btnFirst.text = "Delete"
        holder.btnFirst.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.delete, 0, 0, 0)
        holder.btnFirst.setOnClickListener {
            listener.onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int {
        return dataClassLecturer.size
    }

    class ViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val tvTittle: TextView = itemView.findViewById(R.id.tvTittle)
        val tvData1: TextView = itemView.findViewById(R.id.tvData1)
        val btnFirst: Button = itemView.findViewById(R.id.btnFirst)
        val btnSecond: Button = itemView.findViewById(R.id.btnSecond)
    }

    fun updateRvMajorManager (newDataClassMajorManager: List<DataClassLecturer>) {
        dataClassLecturer.clear()
        dataClassLecturer.addAll(newDataClassMajorManager)
        notifyDataSetChanged()
    }

    interface onItemClickListener {
        fun onDeleteClick(position: Int)
    }

    fun setOnItemClickListener(onClickListener: onItemClickListener) {
        listener = onClickListener
    }
}