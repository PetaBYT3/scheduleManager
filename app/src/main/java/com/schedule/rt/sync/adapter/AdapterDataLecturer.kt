package com.schedule.rt.sync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassLecturer

class AdapterDataLecturer: RecyclerView.Adapter <AdapterDataLecturer.ViewHolder>() {

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
        val currentItem = dataClassLecturer[position]
        holder.tvTittle.text = currentItem.nameLecturer
        holder.tvData1.text = currentItem.nikLecturer

        holder.tvData2.visibility = View.VISIBLE
        holder.tvData2.text = buildString {
            append("Administrator Access : ")
            append(currentItem.administratorAccess)
        }

        holder.btnEdit.setOnClickListener {
            listener.onEditClick(position)
        }

        holder.btnDelete.setOnClickListener {
            listener.onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int {
        return dataClassLecturer.size
    }

    class ViewHolder (itemView: View, listener: onItemClickListener): RecyclerView.ViewHolder(itemView) {
        val tvTittle: TextView = itemView.findViewById(R.id.tvTittle)
        val tvData1: TextView = itemView.findViewById(R.id.tvData1)
        val tvData2: TextView = itemView.findViewById(R.id.tvData2)
        val btnEdit: Button = itemView.findViewById(R.id.btnFirst)
        val btnDelete: Button = itemView.findViewById(R.id.btnSecond)
    }

    fun updateRvLecturer (newDataClassLecturer: List<DataClassLecturer>) {
        dataClassLecturer.clear()
        dataClassLecturer.addAll(newDataClassLecturer)
        notifyDataSetChanged()
    }

    interface onItemClickListener {
        fun onEditClick(position: Int)
        fun onDeleteClick(position: Int)
    }

    fun setOnItemClickListener(onClickListener: onItemClickListener) {
        listener = onClickListener
    }
}