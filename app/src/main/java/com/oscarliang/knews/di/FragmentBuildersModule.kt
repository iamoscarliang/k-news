package com.oscarliang.knews.di

import com.oscarliang.knews.ui.bookmarks.BookmarksFragment
import com.oscarliang.knews.ui.breakingnews.BreakingNewsFragment
import com.oscarliang.knews.ui.newsdetail.NewsDetailFragment
import com.oscarliang.knews.ui.search.SearchFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeBreakingNewsFragment(): BreakingNewsFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment

    @ContributesAndroidInjector
    abstract fun contributeBookmarksFragment(): BookmarksFragment

    @ContributesAndroidInjector
    abstract fun contributeNewsDetailFragment(): NewsDetailFragment

}