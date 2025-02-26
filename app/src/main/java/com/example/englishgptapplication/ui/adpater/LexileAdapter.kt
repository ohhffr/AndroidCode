package com.example.englishgptapplication.ui.adpater

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.englishgptapplication.R
import com.example.englishgptapplication.databinding.ItemLexileBinding

//难度值列表适配器
class LexileAdapter(private val lexiles: List<Int>, private val onLexileSelected: (Int) -> Unit) :
    RecyclerView.Adapter<LexileAdapter.ViewHolder>() {
    private var selectedPosition = 0 // 默认选中第一个位置

    class ViewHolder(val binding: ItemLexileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLexileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.lexile = lexiles[position]
        holder.binding.chipLexile.isChecked = position == selectedPosition

        if (position == selectedPosition) {
            holder.binding.chipLexile.setChipStrokeColorResource(R.color.color_00BFFF)//chip边框变蓝
            holder.binding.chipLexile.setTextColor(holder.itemView.context.getColor(R.color.color_00BFFF))//文本变蓝
            holder.binding.chipLexile.chipStrokeWidth = 2f //chip边框宽度
        } else {
            holder.binding.chipLexile.setChipStrokeColorResource(R.color.color_00000000)
            holder.binding.chipLexile.setTextColor(holder.itemView.context.getColor(R.color.color_605C65))
            holder.binding.chipLexile.chipStrokeWidth = 1f
        }

        holder.binding.chipLexile.setOnClickListener {
            if (position != selectedPosition) {
                val previousSelectedPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousSelectedPosition)
                notifyItemChanged(selectedPosition)
                onLexileSelected(lexiles[position])
            }
        }
    }

    override fun getItemCount() = lexiles.size
}