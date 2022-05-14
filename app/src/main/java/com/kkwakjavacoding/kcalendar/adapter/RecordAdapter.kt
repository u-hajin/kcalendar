package com.kkwakjavacoding.kcalendar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.kkwakjavacoding.kcalendar.databinding.RecyclerviewRowBinding
import com.kkwakjavacoding.kcalendar.fooddatabase.Food
import kotlin.math.roundToInt

class RecordAdapter(val items: ArrayList<Food>) : RecyclerView.Adapter<RecordAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun OnItemClick(data: Food)
        fun deleteClick(data: Food, position: Int)
    }

    var itemClickListener: OnItemClickListener? = null

    inner class ViewHolder(val binding: RecyclerviewRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                itemClickListener?.OnItemClick(items[adapterPosition])
            }

            binding.deleteBtn.setOnClickListener {
                itemClickListener?.deleteClick(items[adapterPosition], adapterPosition)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = RecyclerviewRowBinding.inflate( // row.xml에 대한 RowBinding 만들어져 있다.
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.binding.apply {
            foodName.text = items[position].name
            kcalInfo.text = items[position].kcal.roundToInt().toString()
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }
}