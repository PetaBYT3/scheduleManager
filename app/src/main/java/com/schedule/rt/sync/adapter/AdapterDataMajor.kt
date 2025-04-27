package com.schedule.rt.sync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassMajor
import com.schedule.rt.sync.viewmodel.ViewModelAdministrator

class AdapterDataMajor(
    private val viewModelAdministrator: ViewModelAdministrator,
    private val lifecycleOwner: LifecycleOwner
): RecyclerView.Adapter<AdapterDataMajor.ViewHolder>() {

    val dataClassMajor = mutableListOf<DataClassMajor>()
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
        val currentItem = dataClassMajor[position]
        holder.tvTittle.text = currentItem.nameMajor

        viewModelAdministrator.getManagerSize(currentItem.uidMajor).observe(lifecycleOwner) {
            holder.tvData1.text = buildString {
                append(it.toString())
                append(" Manager")
            }
        }

        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }

        holder.btnEdit.setOnClickListener {
            listener.onEditClick(position)
        }

        holder.btnDelete.setOnClickListener {
            listener.onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int {
        return dataClassMajor.size
    }

    class ViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val tvTittle: TextView = itemView.findViewById(R.id.tvTittle)
        val tvData1: TextView = itemView.findViewById(R.id.tvData1)
        val btnEdit: Button = itemView.findViewById(R.id.btnFirst)
        val btnDelete: Button = itemView.findViewById(R.id.btnSecond)

    }

    fun updateRvMajor(newDataClassMajor: List<DataClassMajor>) {
        dataClassMajor.clear()
        dataClassMajor.addAll(newDataClassMajor)
        notifyDataSetChanged()
    }

    interface onItemClickListener {
        fun onItemClick(position: Int)
        fun onEditClick(position: Int)
        fun onDeleteClick(position: Int)
    }

    fun setOnItemClickListener(onClickListener: onItemClickListener) {
        listener = onClickListener
    }
}