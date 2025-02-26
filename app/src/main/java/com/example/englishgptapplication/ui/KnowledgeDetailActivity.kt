package com.example.englishgptapplication.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.englishgptapplication.R
import com.example.englishgptapplication.databinding.ActivityKnowledgeDetailBinding
import com.example.englishgptapplication.logic.model.ArticleDetail
import com.example.englishgptapplication.ui.adpater.PagerAdapter
import com.example.englishgptapplication.ui.util.AudioPlayer
import com.example.englishgptapplication.ui.viewmodel.DetailViewModel

//第三个页面布局，用于显示知识详情页面，包括文章内容、音频播放、分页控制等功能，同时处理用户的交互事件。
class KnowledgeDetailActivity : AppCompatActivity(), PagerAdapter.OnButtonClickListener {
    private lateinit var binding: ActivityKnowledgeDetailBinding
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var bgmPlayer: AudioPlayer
    private val viewModel by lazy { ViewModelProvider(this)[DetailViewModel::class.java] }
    var currentPage = 0
    var isPaused = false
    // 用于轮询播放进度
    private var currentPlayTime: Float = 0f
    private val updateProgressHandler = Handler(Looper.getMainLooper())
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            currentPlayTime = audioPlayer.getCurrentPosition().toFloat()
            // 使用 payload 局部刷新（避免触发完整绑定）
            (binding.pageRecyclerView.adapter as? PagerAdapter)?.currentPlayTime = currentPlayTime
            binding.pageRecyclerView.adapter?.notifyItemChanged(currentPage, "update_progress")
            updateProgressHandler.postDelayed(this, 100)
        }
    }
    private val speedPreferences by lazy { getSharedPreferences("playback_speed", MODE_PRIVATE) }
    var currentSpeed = 1.0f
    private val fontPreferences by lazy { getSharedPreferences("font_size", MODE_PRIVATE) }
    var currentFontSize = 20f
    // 新增分页模式相关属性
    private val pagingPreferences by lazy { getSharedPreferences("paging_mode", MODE_PRIVATE) }
    var isAutoPaging = false

    // 修改播放完成监听器
    private val completionListener = MediaPlayer.OnCompletionListener {
        runOnUiThread {
            if (isLastPage()) {
                // 最后一页不显示箭头且不自动翻页
                return@runOnUiThread
            }

            // 非最后一页原有逻辑
            (binding.pageRecyclerView.adapter as? PagerAdapter)?.let { _ ->
                val holder = binding.pageRecyclerView.findViewHolderForAdapterPosition(currentPage) as? PagerAdapter.ViewHolder
                holder?.showNavigationHint()
            }

            if (isAutoPaging) {
                Handler(Looper.getMainLooper()).postDelayed({
                    val nextPage = currentPage + 1
                    if (nextPage < (binding.pageRecyclerView.adapter?.itemCount ?: 0)) {
                        (binding.pageRecyclerView.adapter as? PagerAdapter)?.let { _ ->
                            val currentHolder = binding.pageRecyclerView.findViewHolderForAdapterPosition(currentPage) as? PagerAdapter.ViewHolder
                            currentHolder?.hideNavigationHint()
                        }
                        binding.pageRecyclerView.smoothScrollToPosition(nextPage)
                    }
                }, 500)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKnowledgeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        audioPlayer = AudioPlayer()
        bgmPlayer = AudioPlayer()
        currentSpeed = speedPreferences.getFloat("speed", 1.0f)
        currentFontSize = fontPreferences.getFloat("size", 20f)
        isAutoPaging = pagingPreferences.getBoolean("isAuto", false)


        val aid = intent.getIntExtra("aid", 6)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.pageRecyclerView.layoutManager = layoutManager
        // 设置初始空Adapter
        val emptyArticleDetail = ArticleDetail(title = "", contentList = emptyList(), bgmUrl = "")
        binding.pageRecyclerView.adapter = PagerAdapter(emptyList(), emptyArticleDetail).apply {
            setHasStableIds(true)
            listener = this@KnowledgeDetailActivity
        }

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.pageRecyclerView)
        binding.pageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val newPage = layoutManager.findFirstVisibleItemPosition()
                    if (newPage != currentPage) {
                        // 停止旧页面的动画
                        (binding.pageRecyclerView.adapter as? PagerAdapter)?.let {
                            val oldHolder = binding.pageRecyclerView.findViewHolderForAdapterPosition(currentPage) as? PagerAdapter.ViewHolder
                            oldHolder?.hideNavigationHint()
                        }

                        currentPage = newPage
                        binding.pageRecyclerView.adapter?.notifyItemChanged(currentPage)
                        playCurrentPageAudio()
                    }
                }
            }
        })

        viewModel.articleDetail.observe(this) { articleDetail ->
            binding.articleDetail = articleDetail
            binding.pageRecyclerView.adapter = PagerAdapter(articleDetail.contentList, articleDetail).apply {
                setHasStableIds(true)
                listener = this@KnowledgeDetailActivity
            }
            if (articleDetail.bgmUrl.isNotEmpty()) {
                bgmPlayer.playLoop(articleDetail.bgmUrl)
            }
            currentPage = 0
            playCurrentPageAudio()
        }

        viewModel.errorMessage.observe(this) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.loadArticleDetail(aid)

        binding.back.setOnClickListener {
            handleBackAction()
        }
    }

    override fun onReplayClick(position: Int) {
        if (position == currentPage) {
            playCurrentPageAudio()
        }
    }

    override fun onPauseClick(position: Int) {
        if (position == currentPage) {
            isPaused = if (audioPlayer.isPlaying()) {
                audioPlayer.pause()
                true
            } else {
                audioPlayer.start()
                false
            }
            // 使用payload局部刷新
            binding.pageRecyclerView.adapter?.notifyItemChanged(position, "update_icon")
        }
    }

    private fun playCurrentPageAudio() {
        // 在播放前重置状态
        (binding.pageRecyclerView.adapter as? PagerAdapter)?.let { adapter ->
            val holder = binding.pageRecyclerView.findViewHolderForAdapterPosition(currentPage) as? PagerAdapter.ViewHolder
            holder?.hideNavigationHint()
        }

        audioPlayer.setPlaybackSpeed(currentSpeed)
        val articleDetail = viewModel.articleDetail.value ?: return
        if (articleDetail.contentList.size > currentPage) {
            audioPlayer.pause()
            val currentContent = articleDetail.contentList[currentPage]
            audioPlayer.play(currentContent.audioUrl)
            audioPlayer.mediaPlayer.setOnCompletionListener(completionListener) // 添加监听
            isPaused = false
            updateProgressHandler.post(updateProgressRunnable)
            binding.pageRecyclerView.adapter?.notifyItemChanged(currentPage)
        }
    }


    fun updatePlaybackSpeed(speed: Float) {
        currentSpeed = speed
        audioPlayer.setPlaybackSpeed(speed)
        speedPreferences.edit().putFloat("speed", speed).apply()
        // 触发当前页面的速度图标更新
        binding.pageRecyclerView.adapter?.notifyItemChanged(currentPage, "update_speed_icon")
    }

    fun updateFontSize(size: Float) {
        currentFontSize = size
        fontPreferences.edit().putFloat("size", size).apply()
        // 通知当前页面更新字体大小
        binding.pageRecyclerView.adapter?.notifyItemChanged(currentPage, "update_font_size")
    }
    fun updatePagingMode(isAuto: Boolean) {
        isAutoPaging = isAuto
        pagingPreferences.edit().putBoolean("isAuto", isAuto).apply()
    }

    private fun isLastPage(): Boolean {
        val adapter = binding.pageRecyclerView.adapter as? PagerAdapter
        return currentPage == (adapter?.itemCount?.minus(1) ?: 0)
    }

    override fun onBackPressed() {
        handleBackAction()
    }

    private fun handleBackAction() {
        if (isLastPage()) {
            finish()
        } else {
            showExitDialog()
        }
    }
    private fun showExitDialog() {
        val dialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_back_tip)
            .setCancelable(true)
            .create()

        dialog.apply {
            show()
            findViewById<Button>(R.id.confirm_exit)?.setOnClickListener {
                dismiss()
                finish()
            }
            findViewById<Button>(R.id.continue_reading)?.setOnClickListener {
                dismiss()
            }
        }
    }

    fun getLastPageIndex(): Int {
        return (binding.pageRecyclerView.adapter?.itemCount ?: 1) - 1
    }
    override fun onResume() {
        super.onResume()
        audioPlayer.start()
        bgmPlayer.start()
    }

    override fun onPause() {
        super.onPause()
        updateProgressHandler.removeCallbacks(updateProgressRunnable)
        audioPlayer.pause()
        bgmPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        updateProgressHandler.removeCallbacks(updateProgressRunnable)
        audioPlayer.release()
        bgmPlayer.release()
        binding.pageRecyclerView.adapter = null
    }
}