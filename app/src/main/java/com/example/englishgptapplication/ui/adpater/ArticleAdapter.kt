package com.example.englishgptapplication.ui.adpater

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.englishgptapplication.R
import com.example.englishgptapplication.databinding.ItemArticleBinding
import com.example.englishgptapplication.databinding.ItemLoadingBinding
import com.example.englishgptapplication.logic.model.Article

//文章列表适配器
class ArticleAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 定义两种视图类型
    private val ITEM_VIEW_TYPE_ARTICLE = 0
    private val ITEM_VIEW_TYPE_LOADING = 1

    private var showLoading = false
    private val differ = AsyncListDiffer(this, DiffCallback())
    private var onItemClick: ((Article) -> Unit)? = null

    // 文章ViewHolder保持不变
    inner class ArticleViewHolder(val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(differ.currentList[adapterPosition])
                }
            }
        }
    }

    // 新增加载中的ViewHolder
    inner class LoadingViewHolder(binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root) {
        val ivLoading = binding.ivLoading
    }

    override fun getItemViewType(position: Int): Int {
        // 最后一个位置显示加载视图
        return if (showLoading && position == differ.currentList.size) {
            ITEM_VIEW_TYPE_LOADING
        } else {
            ITEM_VIEW_TYPE_ARTICLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_LOADING -> {
                val binding = ItemLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                LoadingViewHolder(binding)
            }
            else -> {
                val binding = ItemArticleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ArticleViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ArticleViewHolder -> {
                if (position >= differ.currentList.size) return
                val article = differ.currentList[position]
                holder.binding.article = article
                article?.let {
                    holder.binding.article = it
                    Glide.with(holder.itemView.context)
                        .load(it.cover)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(holder.binding.ivCover)
                }
            }
            is LoadingViewHolder -> {
                // 使用Glide加载GIF动图
                Glide.with(holder.itemView.context)
                    .asGif()
                    .load(R.drawable.ic_loading_more)
                    .into(holder.ivLoading)
            }
        }
    }

    override fun getItemCount(): Int {
        // 实际数据数量 + 加载视图（如果需要）
        return differ.currentList.size + if (showLoading) 1 else 0
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showLoading(show: Boolean) {
        if (showLoading != show) {
            showLoading = show
            notifyDataSetChanged()
        }
    }

    fun submitList(list: List<Article>) {
        // 不再添加 null，通过 showLoading 控制尾部视图
        differ.submitList(list)
    }
    private class DiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }

    // 新增方法：智能显示加载动画
    fun setLoading(show: Boolean, hasMore: Boolean) {
        if (show && hasMore) {
            showLoading(true)
        } else {
            showLoading(false)
        }
    }
    // 设置子项点击监听器的方法
    fun setOnItemClickListener(listener: (Article) -> Unit) {
        this.onItemClick = listener
    }

}