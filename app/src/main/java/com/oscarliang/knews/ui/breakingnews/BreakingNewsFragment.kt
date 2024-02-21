package com.oscarliang.knews.ui.breakingnews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.oscarliang.knews.AppExecutors
import com.oscarliang.knews.R
import com.oscarliang.knews.binding.FragmentDataBindingComponent
import com.oscarliang.knews.databinding.FragmentBreakingNewsBinding
import com.oscarliang.knews.di.Injectable
import com.oscarliang.knews.ui.common.NewsListAdapter
import com.oscarliang.knews.ui.common.RetryListener
import com.oscarliang.knews.util.DEFAULT_COUNTRY
import com.oscarliang.knews.util.DEFAULT_LANGUAGE
import com.oscarliang.knews.util.DEFAULT_TIME
import com.oscarliang.knews.util.TimeConverter.getTimePassBy
import com.oscarliang.knews.util.autoCleared
import javax.inject.Inject

class BreakingNewsFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    var binding by autoCleared<FragmentBreakingNewsBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent()
    private val viewModel: BreakingNewsViewModel by viewModels {
        viewModelFactory
    }
    private var adapter by autoCleared<NewsListAdapter>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<FragmentBreakingNewsBinding>(
            inflater,
            R.layout.fragment_breaking_news,
            container,
            false
        )
        binding = dataBinding
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            val date = getTimePassBy(DEFAULT_TIME)
            viewModel.setQuery(date, DEFAULT_COUNTRY, DEFAULT_LANGUAGE, 10)
        }

        binding.lifecycleOwner = viewLifecycleOwner
        val rvAdapter = NewsListAdapter(
            dataBindingComponent = dataBindingComponent,
            appExecutors = appExecutors,
            itemClickListener = {
                findNavController()
                    .navigate(
                        BreakingNewsFragmentDirections.actionToNewsDetailFragment(
                            it.id
                        )
                    )
            },
            bookmarkClickListener = {
                viewModel.toggleBookmark(it)
            }
        )
        binding.news = viewModel.news
        binding.listener = object : RetryListener {
            override fun retry() {
                viewModel.refresh()
            }
        }
        binding.newsList.apply {
            adapter = rvAdapter
            itemAnimator?.changeDuration = 0
        }
        this.adapter = rvAdapter
        initRecyclerView()
    }

    private fun initRecyclerView() {
        viewModel.news.observe(viewLifecycleOwner) { news ->
            adapter.submitList(news?.data)
        }
    }

}