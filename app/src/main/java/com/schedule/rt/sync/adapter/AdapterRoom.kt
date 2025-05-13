package com.schedule.rt.sync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassRoom

class AdapterRoom(
    private val tvData1: Boolean? = null,
    private val tvData2: Boolean? = null,
    private val tvData3: Boolean? = null,
    private val tvData4: Boolean? = null,
    private val tvData5: Boolean? = null,
    private val btnFirst: Boolean? = null,
    private val btnSecond: Boolean? = null,
    private val btnNext: Boolean? = null,
    private val onFirstClick: ((DataClassRoom) -> Unit)? = null,
    private val onSecondClick: ((DataClassRoom) -> Unit)? = null,
    private val onNextClick: ((DataClassRoom) -> Unit)? = null
): RecyclerView.Adapter<AdapterRoom.ViewHolder>() {

    var dataClassRoom = mutableListOf<DataClassRoom>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_parent, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.ivItem.setImageResource(R.drawable.room)
        holder.tvData1.visibility = if (tvData1 == true) View.VISIBLE else View.GONE
        holder.tvData2.visibility = if (tvData2 == true) View.VISIBLE else View.GONE
        holder.tvData3.visibility = if (tvData3 == true) View.VISIBLE else View.GONE
        holder.tvData4.visibility = if (tvData4 == true) View.VISIBLE else View.GONE
        holder.tvData5.visibility = if (tvData5 == true) View.VISIBLE else View.GONE

        val currentItem = dataClassRoom[position]
        holder.tvTittle.text = buildString {
            append("Room ")
            append(currentItem.nameRoom)
        }

        holder.btnFirst.visibility = if (btnFirst == true) View.VISIBLE else View.GONE
        holder.btnSecond.visibility = if (btnSecond == true) View.VISIBLE else View.GONE
        holder.btnNext.visibility = if (btnNext == true) View.VISIBLE else View.GONE

        holder.btnFirst.setOnClickListener {
            onFirstClick?.invoke(currentItem)
        }

        holder.btnSecond.setOnClickListener {
            onSecondClick?.invoke(currentItem)
        }

        holder.btnNext.setOnClickListener {
            onNextClick?.invoke(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return dataClassRoom.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val ivItem: ImageView = itemView.findViewById(R.id.ivItem)
        val tvTittle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvData1: TextView = itemView.findViewById(R.id.tvData1)
        val tvData2: TextView = itemView.findViewById(R.id.tvData2)
        val tvData3: TextView = itemView.findViewById(R.id.tvData3)
        val tvData4: TextView = itemView.findViewById(R.id.tvData4)
        val tvData5: TextView = itemView.findViewById(R.id.tvData5)
        val btnFirst: ConstraintLayout = itemView.findViewById(R.id.btnFirst)
        val btnSecond: ConstraintLayout = itemView.findViewById(R.id.btnSecond)
        val btnNext: ConstraintLayout = itemView.findViewById(R.id.btnNext)
    }

    fun updateData(newDataClassRoom: List<DataClassRoom>) {
        dataClassRoom.clear()
        dataClassRoom.addAll(newDataClassRoom)
        notifyDataSetChanged()
    }
}