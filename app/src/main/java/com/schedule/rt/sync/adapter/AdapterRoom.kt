package com.schedule.rt.sync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassRoom

class AdapterRoom: RecyclerView.Adapter<AdapterRoom.ViewHolder>() {

    var dataClassRoom = mutableListOf<DataClassRoom>()
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
        val currentItem = dataClassRoom[position]
        holder.tvTittle.text = buildString {
            append("Room ")
            append(currentItem.nameRoom)
        }

        holder.itemView.setOnClickListener {
            clickListener.onItemClick(position)
        }

        holder.btnEdit.visibility = View.INVISIBLE
        holder.btnDelete.visibility = View.INVISIBLE
    }

    override fun getItemCount(): Int {
        return dataClassRoom.size
    }

    class ViewHolder(itemView: View, clickListener: onItemClickListener): RecyclerView.ViewHolder(itemView) {
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
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(onClickListener: onItemClickListener) {
        clickListener = onClickListener
    }
}