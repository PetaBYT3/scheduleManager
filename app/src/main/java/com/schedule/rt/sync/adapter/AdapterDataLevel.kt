package com.schedule.rt.sync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassLevel
import com.schedule.rt.sync.viewmodel.ViewModelClasses

class AdapterDataLevel(
    private val vmClasses: ViewModelClasses,
    private val lifecycleOwner: LifecycleOwner
): RecyclerView.Adapter<AdapterDataLevel.ViewHolder>() {

    val dataClassLevel = mutableListOf<DataClassLevel>()
    val originalDataClassLevel = mutableListOf<DataClassLevel>()

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
        val currentItem = dataClassLevel[position]
        holder.tvTittle.text = buildString {
            append("Level ")
            append(currentItem.level)
        }
        holder.tvData1.text = buildString {
            append("Semester ")
            append(currentItem.semester)
        }

        holder.tvData2.visibility = View.VISIBLE
        vmClasses.getClassesSizeByLevel(currentItem.uidLevel).observe(lifecycleOwner) {
            holder.tvData2.text = buildString {
                append(it)
                append(" Classes")
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
        return dataClassLevel.size
    }

    class ViewHolder(itemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val tvTittle: TextView = itemView.findViewById(R.id.tvTittle)
        val tvData1: TextView = itemView.findViewById(R.id.tvData1)
        val tvData2: TextView = itemView.findViewById(R.id.tvData2)
        val btnEdit: Button = itemView.findViewById(R.id.btnFirst)
        val btnDelete: Button = itemView.findViewById(R.id.btnSecond)

        init {
            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
            }
        }
    }

    fun updateRvLevel(newDataClassLevel: List<DataClassLevel>) {
        originalDataClassLevel.clear()
        originalDataClassLevel.addAll(newDataClassLevel)

        dataClassLevel.clear()
        dataClassLevel.addAll(newDataClassLevel)
        notifyDataSetChanged()
    }

    fun filterList(query: String) {
        dataClassLevel.clear()

        if (query.isEmpty()) {
            dataClassLevel.addAll(originalDataClassLevel)
        } else {
            val filtered = originalDataClassLevel.filter {
                it.level!!.contains(query, ignoreCase = true) ||
                        it.semester!!.contains(query, ignoreCase = true)
            }
            dataClassLevel.addAll(filtered)
        }

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