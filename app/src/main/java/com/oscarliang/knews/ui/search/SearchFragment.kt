package com.oscarliang.knews.ui.search

import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.oscarliang.knews.AppExecutors
import com.oscarliang.knews.R
import com.oscarliang.knews.binding.FragmentDataBindingComponent
import com.oscarliang.knews.databinding.FragmentSearchBinding
import com.oscarliang.knews.di.Injectable
import com.oscarliang.knews.ui.common.NewsListAdapter
import com.oscarliang.knews.ui.common.RetryListener
import com.oscarliang.knews.util.DEFAULT_COUNTRY
import com.oscarliang.knews.util.DEFAULT_LANGUAGE
import com.oscarliang.knews.util.DEFAULT_TIME
import com.oscarliang.knews.util.TimeConverter.getTimePassBy
import com.oscarliang.knews.util.autoCleared
import javax.inject.Inject

class SearchFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    var binding by autoCleared<FragmentSearchBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent()
    private val viewModel: SearchViewModel by viewModels {
        viewModelFactory
    }
    private var adapter by autoCleared<NewsListAdapter>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<FragmentSearchBinding>(
            inflater,
            R.layout.fragment_search,
            container,
            false
        )
        binding = dataBinding
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.searchResults = viewModel.searchResults
        binding.lifecycleOwner = viewLifecycleOwner
        val rvAdapter = NewsListAdapter(
            dataBindingComponent = dataBindingComponent,
            appExecutors = appExecutors,
            itemClickListener = {
                findNavController()
                    .navigate(
                        SearchFragmentDirections.actionToNewsDetailFragment(
                            it.id
                        )
                    )
            },
            bookmarkClickListener = {
                viewModel.toggleBookmark(it)
            }
        )
        binding.listener = object : RetryListener {
            override fun retry() {
                viewModel.retry()
            }
        }
        binding.newsList.apply {
            adapter = rvAdapter
            itemAnimator?.changeDuration = 0
        }
        this.adapter = rvAdapter
        initRecyclerView()
        initSearchInputListener()
    }

    private fun initRecyclerView() {
        binding.nestedScrollView.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            // Check is scroll to bottom
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                viewModel.loadNextPage()
            }
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { news ->
            adapter.submitList(news?.data)
        }

        viewModel.loadMoreStatus.observe(viewLifecycleOwner) { loadingMore ->
            if (loadingMore == null) {
                binding.loadingMore = false
            } else {
                binding.loadingMore = loadingMore.isRunning
                val error = loadingMore.errorMessageIfNotHandled
                if (error != null) {
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun initSearchInputListener() {
        binding.editSearch.setOnEditorActionListener { view: View, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch(view)
                true
            } else {
                false
            }
        }
        binding.editSearch.setOnKeyListener { view: View, keyCode: Int, event: KeyEvent ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                doSearch(view)
                true
            } else {
                false
            }
        }
    }

    private fun doSearch(v: View) {
        dismissKeyboard(v.windowToken)
        val query = binding.editSearch.text.toString()
        viewModel.setQuery(query, getTimePassBy(DEFAULT_TIME), DEFAULT_COUNTRY, DEFAULT_LANGUAGE, 10)
    }

    private fun dismissKeyboard(windowToken: IBinder) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(windowToken, 0)
    }

}
