package com.oscarliang.knews.ui.newsdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.oscarliang.knews.model.News
import com.oscarliang.knews.repository.NewsRepository
import com.oscarliang.knews.testing.OpenForTesting
import com.oscarliang.knews.util.AbsentLiveData
import javax.inject.Inject

@OpenForTesting
class NewsDetailViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _id: MutableLiveData<Int?> = MutableLiveData()
    val id: LiveData<Int?>
        get() = _id

    val news: LiveData<News> = _id.switchMap { id ->
        if (id == null) {
            AbsentLiveData.create()
        } else {
            repository.getNewsById(id)
        }
    }

    fun setNewsId(id: Int?) {
        if (_id.value != id) {
            _id.value = id
        }
    }

    fun toggleBookmark(news: News) {
        val current = news.bookmark
        val updated = news.copy(bookmark = !current)
        repository.updateNews(updated)
    }

}
