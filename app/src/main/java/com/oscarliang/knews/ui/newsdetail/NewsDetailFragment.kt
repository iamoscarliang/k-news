package com.oscarliang.knews.ui.newsdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.oscarliang.knews.R
import com.oscarliang.knews.binding.FragmentDataBindingComponent
import com.oscarliang.knews.databinding.FragmentNewsDetailBinding
import com.oscarliang.knews.di.Injectable
import com.oscarliang.knews.ui.common.BackListener
import com.oscarliang.knews.util.autoCleared
import javax.inject.Inject

class NewsDetailFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<FragmentNewsDetailBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent()

    private val viewModel: NewsDetailViewModel by viewModels() {
        viewModelFactory
    }
    private val params by navArgs<NewsDetailFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<FragmentNewsDetailBinding>(
            inflater,
            R.layout.fragment_news_detail,
            container,
            false,
            dataBindingComponent
        )
        binding = dataBinding
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setNewsId(params.newsId)
        binding.news = viewModel.news
        binding.viewModel = viewModel
        binding.listener = object : BackListener {
            override fun back() {
                findNavController(this@NewsDetailFragment).navigateUp()
            }
        }
        binding.lifecycleOwner = viewLifecycleOwner
    }

}