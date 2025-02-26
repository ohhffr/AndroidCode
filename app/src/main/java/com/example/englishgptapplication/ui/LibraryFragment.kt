package com.example.englishgptapplication.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishgptapplication.R
import com.example.englishgptapplication.databinding.FragmentLibraryBinding
import com.example.englishgptapplication.logic.model.Article
import com.example.englishgptapplication.logic.model.ArticleListResponse
import com.example.englishgptapplication.ui.adpater.ArticleAdapter
import com.example.englishgptapplication.ui.adpater.LexileAdapter
import com.example.englishgptapplication.ui.adpater.TypeAdapter

import com.example.englishgptapplication.ui.viewmodel.ArticleViewModel
import com.scwang.smart.refresh.header.ClassicsHeader

//第二个页面功能实现，用于展示文章列表，并提供按类型和难度筛选，支持下拉刷新和上拉加载更多功能，同时处理了空数据状态。
class LibraryFragment : Fragment() {
    private lateinit var binding: FragmentLibraryBinding
    private val viewModel by lazy { ViewModelProvider(this)[ArticleViewModel::class.java] }
    private lateinit var articleAdapter: ArticleAdapter

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLibraryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel


        // 初始化Adapter
        articleAdapter = ArticleAdapter()
        binding.rvArticles.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // 获取Header并配置动画
        val header = binding.swipeRefresh.refreshHeader as? ClassicsHeader
        header?.let {
            val imageView = it.findViewById<ImageView>(ClassicsHeader.ID_IMAGE_PROGRESS)
            imageView?.setImageResource(R.drawable.ic_swipe)
        }

        // 设置下拉刷新监听
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
        // 设置上拉加载更多监听
        binding.swipeRefresh.setOnLoadMoreListener {
            viewModel.loadArticles()
        }

        //进入第三个页面
        articleAdapter.setOnItemClickListener {
            startActivity(Intent(requireContext(), KnowledgeDetailActivity::class.java))
        }

        setupObservers()
        return binding.root
    }

    private fun setupObservers() {
        // 监听所有文章数据变化
        viewModel.allArticles.observe(viewLifecycleOwner) { list ->
            // 更新数据时检查空状态
            checkEmptyState(list)
            articleAdapter.submitList(list.toList())
            // 根据是否有更多数据控制Footer
            val hasMore = viewModel.currentPage <= viewModel.totalPages
            binding.swipeRefresh.finishLoadMore(0, true, !hasMore)
        }

        // 在setupObservers中监听ViewModel加载状态
        // 加载状态监听逻辑
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!isLoading) {
                // 加载完成后再次检查空状态
                checkEmptyState(viewModel.allArticles.value)
                // 统一结束刷新状态（包含回弹动画）
                binding.swipeRefresh.finishRefresh(1000)
                binding.swipeRefresh.finishLoadMore(1000)
                stopRotateAnimation(binding.swipeRefresh.refreshHeader as? ClassicsHeader)
            } else {
                binding.tvEmptyHint.visibility = View.GONE
                startRotateAnimation(binding.swipeRefresh.refreshHeader as? ClassicsHeader)
            }
        }

        // 监听文章类型变化
        viewModel.articleTypes.observe(viewLifecycleOwner) { types ->
            binding.rvTypes.adapter = TypeAdapter(types ?: emptyList()) { typeId ->
                viewModel.setType(typeId)
            }
        }
        // 监听难度值的变化
        viewModel.lexileValues.observe(viewLifecycleOwner) { lexiles ->
            binding.rvLexiles.adapter = LexileAdapter(lexiles ?: emptyList()) { lexile ->
                viewModel.setLexile(lexile)
            }
        }
        // 监听文章数据变化
        viewModel.articles.observe(viewLifecycleOwner) { response ->
            response?.let {
                // 检查网络返回的空数据
                checkEmptyState(it.list)
            }
            (binding.rvArticles.adapter as ArticleAdapter).submitList(response?.list ?: emptyList())
        }
    }

    //开始旋转动画
    private fun startRotateAnimation(header: ClassicsHeader?) {
        header?.apply {
            val imageView = findViewById<ImageView>(ClassicsHeader.ID_IMAGE_PROGRESS)
            imageView?.apply {
                clearAnimation()
                startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate_swipe))
            }
        }
    }
    //结束旋转动画
    private fun stopRotateAnimation(header: ClassicsHeader?) {
        header?.apply {
            val imageView = findViewById<ImageView>(ClassicsHeader.ID_IMAGE_PROGRESS)
            imageView?.apply {
                clearAnimation()
                rotation = 0f
            }
        }
    }

    //检查返回的文章是否为空，为空则显示提示文字
    private fun checkEmptyState(list: List<Article>?) {
        val showEmpty = list.isNullOrEmpty() && !viewModel.isLoading.value!!
        binding.tvEmptyHint.visibility = if (showEmpty) View.VISIBLE else View.GONE
    }
}