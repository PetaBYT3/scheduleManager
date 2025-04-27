package com.schedule.rt.sync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassRoom

class AdapterDataRoom: RecyclerView.Adapter<AdapterDataRoom.ViewHolder>() {

    val dataClassRoom = mutableListOf<DataClassRoom>()
    private lateinit var listener : onItemClickListener

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
        val currentItem = dataClassRoom[position]
        holder.tvTittle.text = buildString {
            append("Room ")
            append(currentItem.nameRoom)
        }

        holder.btnEdit.setOnClickListener {
            listener.onEditClick(position)
        }

        holder.btnDelete.setOnClickListener {
            listener.onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int {
        return dataClassRoom.size
    }

    class ViewHolder(itemView: View, listener: onItemClickListener): RecyclerView.ViewHolder(itemView) {
        val tvTittle: TextView = itemView.findViewById(R.id.tvTittle)
        val tvData1: TextView = itemView.findViewById(R.id.tvData1)
        val btnEdit: Button = itemView.findViewById(R.id.btnFirst)
        val btnDelete: Button = itemView.findViewById(R.id.btnSecond)
    }

    fun updateRvRoom(newDataClassRoom: List<DataClassRoom>) {
        dataClassRoom.clear()
        dataClassRoom.addAll(newDataClassRoom)
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