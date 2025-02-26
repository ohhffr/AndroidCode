package com.example.englishgptapplication.ui.adpater

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.SeekBar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.englishgptapplication.R
import com.example.englishgptapplication.databinding.ItemPageBinding
import com.example.englishgptapplication.logic.model.ArticleDetail
import com.example.englishgptapplication.logic.model.Content
import com.example.englishgptapplication.ui.KnowledgeDetailActivity
import com.example.englishgptapplication.ui.util.ToastUtils
import com.lukelorusso.verticalseekbar.VerticalSeekBar

//文章详情页面列表适配器，PagerAdapter 是一个 RecyclerView.Adapter 的子类，
// 用于管理 RecyclerView 中的页面显示和交互，包括音量控制、导航提示动画、按钮点击事件等
class PagerAdapter(
    private var contents: List<Content>,
    private var articleDetail: ArticleDetail,
    var currentPlayTime: Float = 0f // 当前播放时间
) : RecyclerView.Adapter<PagerAdapter.ViewHolder>() {

    interface OnButtonClickListener {
        fun onReplayClick(position: Int)
        fun onPauseClick(position: Int)
    }


    var listener: OnButtonClickListener? = null

    class ViewHolder(val binding: ItemPageBinding) : RecyclerView.ViewHolder(binding.root) {
        var isVolumeExpanded = false // 新增状态变量

        private val audioManager by lazy {
            itemView.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }
        init {
            setupVolumeControls()
        }
        private val handler = Handler(Looper.getMainLooper())
        private var hideMenuRunnable: Runnable? = null
        // 新增动画相关属性
        private var isAnimating = false
        private val animator = ObjectAnimator.ofFloat(binding.paging, "translationX", 0f,
            (-50f).dpToPx()).apply {
            duration = 1000
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        // 显示箭头并启动动画
        fun showNavigationHint() {
            // 如果是最后一页则不显示
            if (adapterPosition == (binding.root.context as KnowledgeDetailActivity).getLastPageIndex()) {
                return
            }
            binding.leftArrow.visibility = View.VISIBLE
            binding.paging.visibility = View.VISIBLE
            if (!isAnimating) {
                animator.start()
                isAnimating = true
            }
        }

        // 隐藏箭头并停止动画
        fun hideNavigationHint() {
            if ((itemView.context as KnowledgeDetailActivity).isAutoPaging) return // 自动模式不自动隐藏

            binding.leftArrow.visibility = View.GONE
            binding.paging.visibility = View.GONE
            if (isAnimating) {
                animator.cancel()
                binding.paging.translationX = 0f
                isAnimating = false
            }
        }

        // dp转换扩展方法
        private fun Float.dpToPx(): Float = this * Resources.getSystem().displayMetrics.density


        fun showMenuTemporarily(menu: CardView) {
            handler.removeCallbacksAndMessages(null)

            // 立即隐藏所有其他菜单（不依赖动画）
            arrayOf(binding.menu, binding.menuFont, binding.menuSpeed, binding.menuPage).forEach {
                if (it != menu && it.visibility == View.VISIBLE) {
                    it.visibility = View.GONE
                    it.alpha = 1f
                    it.clearAnimation()
                }
            }

            // 显示目标菜单
            if (menu.visibility != View.VISIBLE) {
                menu.visibility = View.VISIBLE
                menu.alpha = 0f
                menu.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }

            // 重置自动隐藏计时器
            hideMenuRunnable = Runnable {
                menu.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction { menu.visibility = View.GONE }
                    .start()
            }
            handler.postDelayed(hideMenuRunnable!!, 5000)
        }
        fun clearPendingActions() {
            handler.removeCallbacksAndMessages(null)
            arrayOf(binding.menu, binding.menuFont, binding.menuSpeed).forEach {
                it.animate().cancel()
                it.visibility = View.GONE
                it.alpha = 1f  // 重置透明度
            }
        }
        fun setupSpeedMenu(activity: KnowledgeDetailActivity) {
            with(binding) {
                speedSmall.setOnClickListener { handleSpeedSelection(activity, 0.75f) }
                speedNormal.setOnClickListener { handleSpeedSelection(activity, 1.0f) }
                speedLarge.setOnClickListener { handleSpeedSelection(activity, 1.25f) }

                // 初始化选中状态（不显示Toast）
                updateSpeedChips(activity.currentSpeed, activity, false)
            }
        }
        private fun handleSpeedSelection(activity: KnowledgeDetailActivity, speed: Float) {
            activity.updatePlaybackSpeed(speed)
            updateSpeedChips(speed, activity, true) // 用户操作时显示Toast
            showMenuTemporarily(binding.menuSpeed) // 保持菜单可见
        }

        fun updateSpeedChips(currentSpeed: Float, activity: KnowledgeDetailActivity, showToast: Boolean = false) {
            val selectedColor = ColorStateList.valueOf(activity.getColor(R.color.color_7E6FFE))
            val defaultColor = ColorStateList.valueOf(activity.getColor(R.color.color_D9CFEA))

            with(binding) {
                speedSmall.chipBackgroundColor = if (currentSpeed == 0.75f) selectedColor else defaultColor
                speedNormal.chipBackgroundColor = if (currentSpeed == 1.0f) selectedColor else defaultColor
                speedLarge.chipBackgroundColor = if (currentSpeed == 1.25f) selectedColor else defaultColor
            }
            // 图标更新逻辑
            binding.speedIcon.setImageResource(
                when (currentSpeed) {
                    0.75f -> R.drawable.ic_075speed
                    1.0f -> R.drawable.ic_1speed
                    1.25f -> R.drawable.ic_125speed
                    else -> R.drawable.ic_1speed
                }
            )

            // 仅在需要时显示Toast
            if (showToast) {
                ToastUtils.showToast(activity,"切换成功")
            }
        }

        fun setupFontMenu(activity: KnowledgeDetailActivity) {
            with(binding) {
                fontSmall.setOnClickListener { handleFontSelection(activity, 16f) }
                fontNormal.setOnClickListener { handleFontSelection(activity, 20f) }
                fontLarge.setOnClickListener { handleFontSelection(activity, 24f) }

                // 初始化选中状态（不显示Toast）
                updateFontChips(activity.currentFontSize, activity, false)
                // 初始化字体大小
                tvSentence.textSize = activity.currentFontSize
            }
        }

        private fun handleFontSelection(activity: KnowledgeDetailActivity, size: Float) {
            activity.updateFontSize(size)
            updateFontChips(size, activity, true) // 用户操作时显示Toast
            binding.tvSentence.textSize = size
            showMenuTemporarily(binding.menuFont)
        }

        fun updateFontChips(currentSize: Float, activity: KnowledgeDetailActivity, showToast: Boolean = false) {
            val selectedColor = ColorStateList.valueOf(activity.getColor(R.color.color_7E6FFE))
            val defaultColor = ColorStateList.valueOf(activity.getColor(R.color.color_D9CFEA))

            with(binding) {
                fontSmall.chipBackgroundColor = if (currentSize == 16f) selectedColor else defaultColor
                fontNormal.chipBackgroundColor = if (currentSize == 20f) selectedColor else defaultColor
                fontLarge.chipBackgroundColor = if (currentSize == 24f) selectedColor else defaultColor

                // 更新字体图标
                fontIcon.setImageResource(
                    when (currentSize) {
                        16f -> R.drawable.ic_font_small
                        20f -> R.drawable.ic_font_normal
                        24f -> R.drawable.ic_font_large
                        else -> R.drawable.ic_font_normal
                    }
                )
            }

            // 仅在需要时显示Toast
            if (showToast) {
                ToastUtils.showToast(activity,"切换成功")
            }
        }

        fun setupPagingMenu(activity: KnowledgeDetailActivity) {
            with(binding) {
                // 初始化选中状态
                updatePagingChips(activity.isAutoPaging, activity,false)

                pagingManual.setOnClickListener {
                    handlePagingSelection(activity, false)
                    showMenuTemporarily(menuPage)
                }

                pagingAuto.setOnClickListener {
                    handlePagingSelection(activity, true)
                    showMenuTemporarily(menuPage)
                }

                // 绑定auto图标的点击事件
                auto.setOnClickListener {
                    showMenuTemporarily(menuPage)
                }
            }
        }

        private fun handlePagingSelection(activity: KnowledgeDetailActivity, isAuto: Boolean) {
            activity.updatePagingMode(isAuto)
            updatePagingChips(isAuto, activity,true)
        }

        private fun updatePagingChips(isAuto: Boolean, activity: KnowledgeDetailActivity,showToast: Boolean = false) {
            val selectedColor = ColorStateList.valueOf(activity.getColor(R.color.color_7E6FFE))
            val defaultColor = ColorStateList.valueOf(activity.getColor(R.color.color_D9CFEA))

            with(binding) {
                pagingManual.chipBackgroundColor = if (!isAuto) selectedColor else defaultColor
                pagingAuto.chipBackgroundColor = if (isAuto) selectedColor else defaultColor

                pagingIcon.setImageResource(
                    if (isAuto) R.drawable.ic_auto_paging
                    else R.drawable.ic_manual_paging
                )
            }
            if (showToast){
                ToastUtils.showToast(activity,"切换成功")
            }
        }

        private fun setupVolumeControls() {

            binding.volume.setOnClickListener {
                isVolumeExpanded = !isVolumeExpanded
                updateVolumeUI()
            }

            // 初始化VerticalSeekBar
            binding.seekBar.maxValue = 100
            updateSeekBarProgress()

            // VerticalSeekBar拖动监听
            binding.seekBar.setOnProgressChangeListener { progress ->
                setSystemVolume(progress)
            }

            binding.addVolume.setOnClickListener {
                val newProgress = binding.seekBar.progress + 5
                binding.seekBar.progress = newProgress.coerceAtMost(100)
                setSystemVolume(newProgress)
            }

            binding.subVolume.setOnClickListener {
                val newProgress = binding.seekBar.progress - 5
                binding.seekBar.progress = newProgress.coerceAtLeast(0)
                setSystemVolume(newProgress)
            }
        }

        fun updateVolumeUI() {
            // 更新按钮样式
            if (isVolumeExpanded) {
                binding.volume.setBackgroundResource(R.drawable.shape_round01)
                binding.volume.setImageResource(R.drawable.ic_volume_checked)
                binding.volumeUi.visibility = View.VISIBLE
            } else {
                binding.volume.setBackgroundResource(R.drawable.shape_round02)
                binding.volume.setImageResource(R.drawable.ic_volume)
                binding.volumeUi.visibility = View.GONE
            }
        }

        private fun setSystemVolume(progress: Int) {
            if (isVolumeExpanded) return
            isVolumeExpanded = true

            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = (progress / 100f * maxVolume).toInt()
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                targetVolume,
                AudioManager.FLAG_SHOW_UI
            )
            updateSeekBarProgress()

            isVolumeExpanded = false
        }

        private fun updateSeekBarProgress() {
            if (isVolumeExpanded) return
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            binding.seekBar.progress = (currentVolume.toFloat() / maxVolume * 100).toInt()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = contents[position]
        holder.binding.content = content
        holder.binding.articleDetail = articleDetail

        // 设置按钮点击监听
        holder.binding.replay.setOnClickListener { listener?.onReplayClick(position) }
        holder.binding.pause.setOnClickListener { listener?.onPauseClick(position) }
        holder.itemView.setOnClickListener {holder.showMenuTemporarily(holder.binding.menu) }
        holder.binding.speed.setOnClickListener { holder.showMenuTemporarily(holder.binding.menuSpeed)}
        holder.binding.fontSize.setOnClickListener { holder.showMenuTemporarily(holder.binding.menuFont) }


        // 处理图片加载（添加缓存检查）
        val currentImgUrl = content.imgUrl
        if (holder.binding.image.tag != currentImgUrl) {
            Glide.with(holder.itemView.context)
                .load(currentImgUrl)
                .into(holder.binding.image)
            holder.binding.image.tag = currentImgUrl
        }

        //更新句子高亮
        updateSentenceHighlight(holder, content)

        updateButtonIcon(holder, position)

        val activity = holder.itemView.context as? KnowledgeDetailActivity
        activity?.let {
            holder.setupSpeedMenu(it)
        }

        activity?.let {
            holder.setupPagingMenu(it)
            holder.setupSpeedMenu(it)
            holder.setupFontMenu(it)
            holder.binding.tvSentence.textSize = it.currentFontSize
        }
        holder.binding.executePendingBindings()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            // 根据 payload 类型局部更新
            when (payloads.first()) {
                "update_progress" -> {
                    // 仅更新播放进度相关 UI（高亮和按钮图标）
                    updateSentenceHighlight(holder, contents[position])
                    updateButtonIcon(holder, position)
                }
                "update_speed_icon" -> {
                    val activity = holder.itemView.context as? KnowledgeDetailActivity
                    activity?.let {
                        holder.updateSpeedChips(it.currentSpeed, it, false) // 不显示Toast
                    }
                }
                "update_font_size" -> {
                    val activity = holder.itemView.context as? KnowledgeDetailActivity
                    activity?.let {
                        holder.updateFontChips(it.currentFontSize, it, false) // 不显示Toast
                        holder.binding.tvSentence.textSize = it.currentFontSize
                    }
                }
                "update_icon" -> updateButtonIcon(holder, position)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount() = contents.size

    override fun getItemId(position: Int) = contents[position].hashCode().toLong()

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.clearPendingActions()
        holder.hideNavigationHint()
        holder.clearPendingActions()
        // 重置音量控制状态
        holder.isVolumeExpanded = false
        holder.updateVolumeUI()
    }

    private fun updateButtonIcon(holder: ViewHolder, position: Int) {
        val activity = holder.itemView.context as? KnowledgeDetailActivity
        val isCurrentPage = (activity?.currentPage == position)
        if (isCurrentPage && activity != null) {
            holder.binding.pause.setImageResource(
                if (activity.isPaused) R.drawable.ic_play else R.drawable.ic_pause
            )
        } else {
            holder.binding.pause.setImageResource(R.drawable.ic_pause)
        }
    }

    //根据播放时间高亮单词
    private fun updateSentenceHighlight(holder: ViewHolder, content: Content) {
        // 仅在播放时间变化时更新高亮
        if (holder.binding.tvSentence.tag != currentPlayTime) {
            val sentence = content.sentence
            val spannable = SpannableString(sentence)
            val wordSegments = content.sentenceByXFList

            var currentIndex = 0
            wordSegments.forEach { segment ->
                val start = sentence.indexOf(segment.word, currentIndex)
                if (start != -1) {
                    val end = start + segment.word.length
                    if (currentPlayTime >= segment.wb && currentPlayTime <= segment.we) {
                        spannable.setSpan(
                            ForegroundColorSpan(Color.parseColor("#FFA500")),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    currentIndex = end
                }
            }
            holder.binding.tvSentence.text = spannable
            holder.binding.tvSentence.tag = currentPlayTime // 缓存当前播放时间
        }
    }
}