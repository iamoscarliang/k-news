package com.oscarliang.knews.util

import androidx.lifecycle.MutableLiveData
import com.oscarliang.knews.api.ApiEmptyResponse
import com.oscarliang.knews.api.ApiErrorResponse
import com.oscarliang.knews.api.ApiResponse
import com.oscarliang.knews.api.ApiSuccessResponse
import com.oscarliang.knews.api.NewsService
import com.oscarliang.knews.db.NewsDatabase
import com.oscarliang.knews.model.NewsSearchResult
import java.io.IOException

class FetchNextSearchPageTask(
    private val query: String,
    private val date: String,
    private val country: String,
    private val language: String,
    private val number: Int,
    private val newsService: NewsService,
    private val db: NewsDatabase
) : Runnable {

    private val _liveData = MutableLiveData<Resource<Boolean>?>()
    val liveData: MutableLiveData<Resource<Boolean>?> = _liveData

    override fun run() {
        val current = db.newsDao().getSearchResultCount(query)
        if (current == 0) {
            _liveData.postValue(null)
            return
        }
        if (current % number != 0) {
            _liveData.postValue(Resource.success(false))
            return
        }
        val newValue = try {
            val response = newsService.search(
                query = query,
                date = date,
                country = country,
                language = language,
                number = number,
                offset = current
            ).execute()
            when (val apiResponse = ApiResponse.create(response)) {
                is ApiSuccessResponse -> {
                    // We merge all new search result into current result list
                    val merged = apiResponse.body.news.mapIndexed { index, news ->
                        NewsSearchResult(news.id, query, index + current)
                    }
                    db.runInTransaction {
                        db.newsDao().insertNews(apiResponse.body.news)
                        db.newsDao().insertNewsSearchResults(merged)
                    }
                    Resource.success(apiResponse.body.number == number)
                }

                is ApiEmptyResponse -> {
                    Resource.success(false)
                }

                is ApiErrorResponse -> {
                    Resource.error(apiResponse.errorMessage, true)
                }
            }

        } catch (e: IOException) {
            Resource.error(e.message!!, true)
        }
        _liveData.postValue(newValue)
    }

}