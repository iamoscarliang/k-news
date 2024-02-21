package com.oscarliang.knews.ui.breakingnews

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.oscarliang.knews.model.News
import com.oscarliang.knews.repository.NewsRepository
import com.oscarliang.knews.testing.OpenForTesting
import com.oscarliang.knews.util.AbsentLiveData
import com.oscarliang.knews.util.Resource
import javax.inject.Inject

@OpenForTesting
class BreakingNewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _query: MutableLiveData<Query> = MutableLiveData()
    val query: LiveData<Query>
        get() = _query

    val news: LiveData<Resource<List<News>>> = _query.switchMap { input ->
        input.ifExists { date, country, language, number, forceFetch ->
            repository.getBreakingNews(
                date = date,
                country = country,
                language = language,
                number = number,
                forceFetch = forceFetch
            )
        }
    }

    fun setQuery(date: String, country: String, language: String, number: Int) {
        val update = Query(date, country, language, number, false)
        if (_query.value == update) {
            return
        }
        _query.value = update
    }

    fun refresh() {
        _query.value?.let {
            _query.value = it.copy(forceFetch = true)
        }
    }

    fun toggleBookmark(news: News) {
        val current = news.bookmark
        val updated = news.copy(bookmark = !current)
        repository.updateNews(updated)
    }

    data class Query(
        val date: String,
        val country: String,
        val language: String,
        val number: Int,
        val forceFetch: Boolean
    ) {
        fun <T> ifExists(f: (String, String, String, Int, Boolean) -> LiveData<T>): LiveData<T> {
            return if (date.isBlank() || number == 0) {
                AbsentLiveData.create()
            } else {
                f(date, country, language, number, forceFetch)
            }
        }
    }

}