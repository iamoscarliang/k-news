package com.oscarliang.knews.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.oscarliang.knews.model.News
import com.oscarliang.knews.repository.NewsRepository
import com.oscarliang.knews.testing.OpenForTesting
import com.oscarliang.knews.util.AbsentLiveData
import com.oscarliang.knews.util.Resource
import com.oscarliang.knews.util.State
import javax.inject.Inject

@OpenForTesting
class SearchViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _query: MutableLiveData<Query> = MutableLiveData()
    private val nextPageHandler = NextPageHandler(repository)

    val query: LiveData<Query>
        get() = _query

    val searchResults: LiveData<Resource<List<News>>> = _query.switchMap { input ->
        input.ifExists { query, date, country, language, number, forceFetch ->
            repository.search(
                query = query,
                date = date,
                country = country,
                language = language,
                number = number,
                forceFetch = forceFetch
            )
        }
    }

    val loadMoreStatus: LiveData<LoadMoreState>
        get() = nextPageHandler.loadMoreState

    fun setQuery(query: String, date: String, country: String, language: String, number: Int) {
        val update = Query(query, date, country, language, number, true)
        if (_query.value == update) {
            return
        }
        nextPageHandler.reset()
        _query.value = update
    }

    fun loadNextPage() {
        _query.value?.let {
            if (it.query.isNotBlank() && it.date.isNotBlank()) {
                nextPageHandler.queryNextPage(it.query, it.date, it.country, it.language, it.number)
            }
        }
    }

    fun retry() {
        _query.value?.let {
            _query.value = it
        }
    }

    fun toggleBookmark(news: News) {
        val current = news.bookmark
        val updated = news.copy(bookmark = !current)
        repository.updateNews(updated)
    }

    data class Query(
        val query: String,
        val date: String,
        val country: String,
        val language: String,
        val number: Int,
        val forceFetch: Boolean
    ) {
        fun <T> ifExists(f: (String, String, String, String, Int, Boolean) -> LiveData<T>): LiveData<T> {
            return if (date.isBlank() || number == 0) {
                AbsentLiveData.create()
            } else {
                f(query, date, country, language, number, forceFetch)
            }
        }
    }

    class LoadMoreState(val isRunning: Boolean, val errorMessage: String?) {
        private var handledError = false

        val errorMessageIfNotHandled: String?
            get() {
                if (handledError) {
                    return null
                }
                handledError = true
                return errorMessage
            }
    }

    class NextPageHandler(
        private val repository: NewsRepository
    ) : Observer<Resource<Boolean>?> {

        val loadMoreState = MutableLiveData<LoadMoreState>()
        private var nextPageLiveData: LiveData<Resource<Boolean>?>? = null
        private var query: String? = null
        private var _hasMore: Boolean = false
        val hasMore
            get() = _hasMore

        init {
            reset()
        }

        fun queryNextPage(
            query: String,
            date: String,
            country: String,
            language: String,
            number: Int
        ) {
            if (this.query == query) {
                return
            }
            unregister()
            this.query = query
            nextPageLiveData = repository.searchNextPage(query, date, country, language, number)
            loadMoreState.value = LoadMoreState(
                isRunning = true,
                errorMessage = null
            )
            nextPageLiveData?.observeForever(this)
        }

        override fun onChanged(value: Resource<Boolean>?) {
            if (value == null) {
                reset()
            } else {
                when (value.state) {
                    State.SUCCESS -> {
                        _hasMore = value.data == true
                        unregister()
                        loadMoreState.setValue(
                            LoadMoreState(
                                isRunning = false,
                                errorMessage = null
                            )
                        )
                    }

                    State.ERROR -> {
                        _hasMore = true
                        unregister()
                        loadMoreState.setValue(
                            LoadMoreState(
                                isRunning = false,
                                errorMessage = value.message
                            )
                        )
                    }

                    State.LOADING -> {
                        // ignore
                    }
                }
            }
        }

        private fun unregister() {
            nextPageLiveData?.removeObserver(this)
            nextPageLiveData = null
            if (_hasMore) {
                query = null
            }
        }

        fun reset() {
            unregister()
            _hasMore = true
            loadMoreState.value = LoadMoreState(
                isRunning = false,
                errorMessage = null
            )
        }
    }

}
