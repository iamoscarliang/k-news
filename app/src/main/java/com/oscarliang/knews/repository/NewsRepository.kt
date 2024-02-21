package com.oscarliang.knews.repository

import androidx.lifecycle.LiveData
import com.oscarliang.knews.AppExecutors
import com.oscarliang.knews.api.ApiResponse
import com.oscarliang.knews.api.NewsSearchResponse
import com.oscarliang.knews.api.NewsService
import com.oscarliang.knews.db.NewsDao
import com.oscarliang.knews.db.NewsDatabase
import com.oscarliang.knews.model.BreakingNews
import com.oscarliang.knews.model.News
import com.oscarliang.knews.model.NewsSearchResult
import com.oscarliang.knews.testing.OpenForTesting
import com.oscarliang.knews.util.FetchNextSearchPageTask
import com.oscarliang.knews.util.NetworkBoundResource
import com.oscarliang.knews.util.RateLimiter
import com.oscarliang.knews.util.Resource
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OpenForTesting
class NewsRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val db: NewsDatabase,
    private val newsDao: NewsDao,
    private val newsService: NewsService
) {

    private val newsRateLimit = RateLimiter<String>(10, TimeUnit.MINUTES)

    fun getBreakingNews(
        date: String,
        country: String,
        language: String,
        number: Int,
        forceFetch: Boolean
    ): LiveData<Resource<List<News>>> {
        return object : NetworkBoundResource<List<News>, NewsSearchResponse>(appExecutors) {
            override fun saveCallResult(item: NewsSearchResponse) {
                val breakingNews = item.news.map {
                    BreakingNews(it.id)
                }
                db.runInTransaction {
                    newsDao.deleteAllBreakingNews()
                    newsDao.insertNews(item.news)
                    newsDao.insertBreakingNews(breakingNews)
                }
            }

            override fun shouldFetch(data: List<News>?): Boolean {
                return data.isNullOrEmpty() || newsRateLimit.shouldFetch(date) || forceFetch
            }

            override fun loadFromDb(): LiveData<List<News>> {
                return newsDao.getBreakingNews()
            }

            override fun createCall(): LiveData<ApiResponse<NewsSearchResponse>> {
                return newsService.getBreakingNews(
                    date = date,
                    country = country,
                    language = language,
                    number = number
                )
            }

            override fun onFetchFailed() {
                newsRateLimit.reset(date)
            }
        }.asLiveData()
    }

    fun search(
        query: String,
        date: String,
        country: String,
        language: String,
        number: Int,
        forceFetch: Boolean
    ): LiveData<Resource<List<News>>> {
        return object : NetworkBoundResource<List<News>, NewsSearchResponse>(appExecutors) {
            override fun saveCallResult(item: NewsSearchResponse) {
                val searchResult = item.news.mapIndexed { index, news ->
                    NewsSearchResult(news.id, query, index)
                }
                db.runInTransaction {
                    newsDao.deleteAllNewsSearchResults(query)
                    newsDao.insertNews(item.news)
                    newsDao.insertNewsSearchResults(searchResult)
                }
            }

            override fun shouldFetch(data: List<News>?): Boolean {
                return forceFetch
            }

            override fun loadFromDb(): LiveData<List<News>> {
                return newsDao.search(query)
            }

            override fun createCall(): LiveData<ApiResponse<NewsSearchResponse>> {
                return newsService.search(
                    query = query,
                    date = date,
                    country = country,
                    language = language,
                    number = number
                )
            }
        }.asLiveData()
    }

    fun searchNextPage(
        query: String,
        date: String,
        country: String,
        language: String,
        number: Int
    ): LiveData<Resource<Boolean>?> {
        val fetchNextSearchPageTask = FetchNextSearchPageTask(
            query = query,
            date = date,
            country = country,
            language = language,
            number = number,
            newsService = newsService,
            db = db
        )
        appExecutors.networkIO().execute(fetchNextSearchPageTask)
        return fetchNextSearchPageTask.liveData
    }

    fun getBookmarks(): LiveData<List<News>> {
        return newsDao.getBookmarks()
    }

    fun getNewsById(id: Int): LiveData<News> {
        return newsDao.getNewsById(id)
    }

    fun updateNews(news: News) {
        appExecutors.diskIO().execute {
            newsDao.updateNews(news)
        }
    }

}
