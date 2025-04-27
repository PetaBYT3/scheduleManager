package com.schedule.rt.sync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassBuilding
import com.schedule.rt.sync.viewmodel.ViewModelBuilding

class AdapterBuilding(
    private val viewModelBuilding: ViewModelBuilding,
    private val lifecycleOwner: LifecycleOwner
): RecyclerView.Adapter<AdapterBuilding.ViewHolder>() {

    var dataClassBuilding = mutableListOf<DataClassBuilding>()
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
        val currentItem = dataClassBuilding[position]
        holder.tvTittle.text = buildString {
            append("Building ")
            append(currentItem.nameBuilding)
        }

        viewModelBuilding.getRoomSize(currentItem.uidBuilding).observe(lifecycleOwner) {
            holder.tvData1.text = buildString {
                append(it.toString())
                append(" Room")
            }
        }

        holder.itemView.setOnClickListener {
            clickListener.onItemClick(position)
        }

        holder.btnEdit.visibility = View.INVISIBLE
        holder.btnDelete.visibility = View.INVISIBLE
    }

    override fun getItemCount(): Int {
        return dataClassBuilding.size
    }

    class ViewHolder(itemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val tvTittle: TextView = itemView.findViewById(R.id.tvTittle)
        val tvData1: TextView = itemView.findViewById(R.id.tvData1)
        val btnEdit: Button = itemView.findViewById(R.id.btnFirst)
        val btnDelete: Button = itemView.findViewById(R.id.btnSecond)
    }

    fun updateRvBuilding(newDataClassBuilding: List<DataClassBuilding>) {
        dataClassBuilding.clear()
        dataClassBuilding.addAll(newDataClassBuilding)
        notifyDataSetChanged()
    }

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(onClickListener: onItemClickListener) {
        clickListener = onClickListener
    }
}