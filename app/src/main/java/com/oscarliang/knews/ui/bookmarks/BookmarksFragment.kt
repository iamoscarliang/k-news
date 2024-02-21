package com.oscarliang.knews.ui.bookmarks

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
import com.oscarliang.knews.databinding.FragmentBookmarksBinding
import com.oscarliang.knews.di.Injectable
import com.oscarliang.knews.ui.common.NewsListAdapter
import com.oscarliang.knews.util.autoCleared
import javax.inject.Inject

class BookmarksFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    var binding by autoCleared<FragmentBookmarksBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent()
    private val viewModel: BookmarksViewModel by viewModels() {
        viewModelFactory
    }
    private var adapter by autoCleared<NewsListAdapter>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<FragmentBookmarksBinding>(
            inflater,
            R.layout.fragment_bookmarks,
            container,
            false
        )
        binding = dataBinding
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bookmarks = viewModel.bookmarks
        binding.lifecycleOwner = viewLifecycleOwner
        val rvAdapter = NewsListAdapter(
            dataBindingComponent = dataBindingComponent,
            appExecutors = appExecutors,
            itemClickListener = {
                findNavController()
                    .navigate(
                        BookmarksFragmentDirections.actionToNewsDetailFragment(
                            it.id
                        )
                    )
            },
            bookmarkClickListener = {
                viewModel.toggleBookmark(it)
            }
        )
        binding.newsList.apply {
            adapter = rvAdapter
            itemAnimator?.changeDuration = 0
        }
        this.adapter = rvAdapter
        initRecyclerView()
    }

    private fun initRecyclerView() {
        viewModel.bookmarks.observe(viewLifecycleOwner) { news ->
            adapter.submitList(news)
        }
    }

}