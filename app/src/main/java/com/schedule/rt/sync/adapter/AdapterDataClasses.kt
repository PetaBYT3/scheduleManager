package com.schedule.rt.sync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassClasses
import com.schedule.rt.sync.viewmodel.ViewModelCourse

class AdapterDataClasses(
    val vmCourse: ViewModelCourse,
    val lifecycleOwner: LifecycleOwner
): RecyclerView.Adapter<AdapterDataClasses.ViewHolder>() {

    val dataClassClasses = mutableListOf<DataClassClasses>()

    private lateinit var clickListener : onItemClickListener

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
        val currentItem = dataClassClasses[position]
        holder.tvTittle.text = buildString {
            append("Class ")
            append(currentItem.nameClasses)
        }

        vmCourse.getCourseSizeByClasses(currentItem.uidClasses.toString()).observe(lifecycleOwner) {
            holder.tvData1.text = buildString {
                append(it)
                append(" Course")
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
        return dataClassClasses.size
    }

    class ViewHolder(itemView: View, clickListener : onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val tvTittle: TextView = itemView.findViewById(R.id.tvTittle)
        val tvData1: TextView = itemView.findViewById(R.id.tvData1)
        val btnEdit : Button = itemView.findViewById(R.id.btnFirst)
        val btnDelete : Button = itemView.findViewById(R.id.btnSecond)

        init {
            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
            }
        }
    }

    fun updateRvClasses(newDataClassClasses: List<DataClassClasses>) {
        dataClassClasses.clear()
        dataClassClasses.addAll(newDataClassClasses)
        notifyDataSetChanged()
    }

    interface onItemClickListener {
        fun onItemClick(position: Int)
        fun onEditClick(position: Int)
        fun onDeleteClick(position: Int)
    }

    fun setOnItemClickListener(onClickListener: onItemClickListener) {
        clickListener = onClickListener
    }
}