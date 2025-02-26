package com.example.englishgptapplication.ui.adpater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.englishgptapplication.R
import com.example.englishgptapplication.databinding.ItemTypeBinding
import com.example.englishgptapplication.logic.model.ArticleType

//文章类型列表适配器
class TypeAdapter(private val items: List<ArticleType>, private val onClick: (Int) -> Unit) :
    RecyclerView.Adapter<TypeAdapter.ViewHolder>() {

    private var selectedPosition = 0 // 默认选中第一项
    init {
        // 初始化时立即刷新首项选中状态
        notifyItemChanged(selectedPosition)
    }

    class ViewHolder(val binding: ItemTypeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 设置数据
        if (position == 0) {
            // 第一项，设置为“精品阅读”
            holder.binding.type = null
        } else {
            // 其他项，显示列表中的数据
            holder.binding.type = items[position - 1]
        }

        // 根据选中状态设置背景和文字颜色
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
            holder.binding.typeItem.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.color_00BFFF))
            holder.binding.selectorBar.visibility = View.VISIBLE // 显示拖条
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.color_F0F0F5))
            holder.binding.typeItem.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.color_605C65))
            holder.binding.selectorBar.visibility = View.GONE // 隐藏拖条
        }

        // 点击事件
        holder.itemView.setOnClickListener {
            // 更新选中位置
            if (selectedPosition != position) {
                val previousSelectedPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousSelectedPosition) // 更新之前的选中项
                notifyItemChanged(selectedPosition) // 更新当前的选中项
            }
            if (position == 0) {
                onClick(0) // -1 表示固定项的标识
            } else {
                onClick(items[position - 1].id)
            }
        }
    }

    override fun getItemCount() = items.size + 1 // 加1是因为第一项是固定的“精品阅读”
}