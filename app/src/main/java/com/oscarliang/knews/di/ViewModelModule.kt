package com.oscarliang.knews.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.oscarliang.knews.ui.bookmarks.BookmarksViewModel
import com.oscarliang.knews.ui.breakingnews.BreakingNewsViewModel
import com.oscarliang.knews.ui.newsdetail.NewsDetailViewModel
import com.oscarliang.knews.ui.search.SearchViewModel
import com.oscarliang.knews.viewmodel.ViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(BreakingNewsViewModel::class)
    abstract fun bindBreakingNewsViewModel(viewModel: BreakingNewsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindSearchViewModel(viewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BookmarksViewModel::class)
    abstract fun bindBookmarksViewModel(viewModel: BookmarksViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NewsDetailViewModel::class)
    abstract fun bindNewsDetailViewModel(viewModel: NewsDetailViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

}
