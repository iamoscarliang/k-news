package com.oscarliang.knews.ui.bookmarks

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.oscarliang.knews.model.News
import com.oscarliang.knews.repository.NewsRepository
import com.oscarliang.knews.testing.OpenForTesting
import javax.inject.Inject

@OpenForTesting
class BookmarksViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    val bookmarks:LiveData<List<News>> = repository.getBookmarks()

    fun toggleBookmark(news: News) {
        val current = news.bookmark
        val updated = news.copy(bookmark = !current)
        repository.updateNews(updated)
    }

}