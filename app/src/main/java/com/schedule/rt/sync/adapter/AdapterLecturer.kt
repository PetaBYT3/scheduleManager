package com.schedule.rt.sync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassLecturer
import com.schedule.rt.sync.viewmodel.ViewModelAdministrator

class AdapterLecturer(
    private val viewModelAdministrator: ViewModelAdministrator,
    private val lifecycleOwner: LifecycleOwner
): RecyclerView.Adapter<AdapterLecturer.ViewHolder>() {

    var dataClassLecturer = mutableListOf<DataClassLecturer>()
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
        holder.btnSecond.visibility = View.INVISIBLE

        val currentItem = dataClassLecturer[position]
        holder.tvTittle.text = currentItem.nameLecturer
        holder.tvData1.text = currentItem.nikLecturer

        holder.tvData2.visibility = View.VISIBLE
        viewModelAdministrator.getMajorManager(currentItem.uidMajorManager).observe(lifecycleOwner) {
            if (it != null) {
                holder.tvData2.text = it.nameMajor
            } else {
                holder.tvData2.text = buildString {
                    append("-")
                }
            }
        }

        holder.btnFirst.text = "Add"
        holder.btnFirst.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.add, 0, 0, 0)
        holder.btnFirst.setOnClickListener {
            clickListener.onAddClick(position)
        }
    }

    override fun getItemCount(): Int {
        return dataClassLecturer.size
    }

    class ViewHolder(itemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val tvTittle: TextView = itemView.findViewById(R.id.tvTittle)
        val tvData1: TextView = itemView.findViewById(R.id.tvData1)
        val tvData2: TextView = itemView.findViewById(R.id.tvData2)
        val btnFirst: Button = itemView.findViewById(R.id.btnFirst)
        val btnSecond: Button = itemView.findViewById(R.id.btnSecond)
    }

    fun updateRvLecturer (newDataClassLecturer: List<DataClassLecturer>) {
        dataClassLecturer.clear()
        dataClassLecturer.addAll(newDataClassLecturer)
        notifyDataSetChanged()
    }

    interface onItemClickListener {
        fun onAddClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        clickListener = listener
    }
}