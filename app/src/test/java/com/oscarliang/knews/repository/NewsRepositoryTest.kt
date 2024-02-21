package com.oscarliang.knews.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.oscarliang.knews.api.ApiResponse
import com.oscarliang.knews.api.NewsSearchResponse
import com.oscarliang.knews.api.NewsService
import com.oscarliang.knews.db.NewsDao
import com.oscarliang.knews.db.NewsDatabase
import com.oscarliang.knews.model.News
import com.oscarliang.knews.util.AbsentLiveData
import com.oscarliang.knews.util.ApiUtil.successCall
import com.oscarliang.knews.util.InstantAppExecutors
import com.oscarliang.knews.util.Resource
import com.oscarliang.knews.util.TestUtil
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import retrofit2.Response

@RunWith(JUnit4::class)
class NewsRepositoryTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: NewsRepository
    private val dao = mock(NewsDao::class.java)
    private val service = mock(NewsService::class.java)

    @Before
    fun init() {
        val db = mock(NewsDatabase::class.java)
        `when`(db.newsDao()).thenReturn(dao)
        `when`(db.runInTransaction(any())).thenCallRealMethod()
        repository = NewsRepository(
            appExecutors = InstantAppExecutors(),
            db = db,
            newsDao = dao,
            newsService = service
        )
    }

    @Test
    fun loadBreakingNewsFromNetwork() {
        val dbData = MutableLiveData<List<News>>()
        `when`(dao.getBreakingNews()).thenReturn(dbData)

        val news = TestUtil.createNews(3, "a", "b", "c", "d")
        val result = NewsSearchResponse(3, news)
        val call = successCall(result)
        `when`(service.getBreakingNews("2024-02-16 16:12:35", "us", "en", 10))
            .thenReturn(call)

        val data = repository
            .getBreakingNews("2024-02-16 16:12:35", "us", "en", 10, false)
        verify(dao).getBreakingNews()
        verifyNoMoreInteractions(service)

        val observer = mock<Observer<Resource<List<News>>>>()
        data.observeForever(observer)
        verifyNoMoreInteractions(service)
        verify(observer).onChanged(Resource.loading(null))
        val updatedDbData = MutableLiveData<List<News>>()
        `when`(dao.getBreakingNews()).thenReturn(updatedDbData)

        dbData.postValue(null)
        verify(service).getBreakingNews("2024-02-16 16:12:35", "us", "en", 10)
        verify(dao).insertNews(news)

        updatedDbData.postValue(news)
        verify(observer).onChanged(Resource.success(news))
    }

    @Test
    fun searchNextPageNull() {
        `when`(dao.getSearchResultCount("foo")).thenReturn(0)
        val observer = mock<Observer<Resource<Boolean>?>>()
        repository.searchNextPage("foo", "2024-02-16 16:12:35", "us", "en", 10)
            .observeForever(observer)
        verify(observer).onChanged(null)
    }

    @Test
    fun searchFromDb() {
        val observer = mock<Observer<Resource<List<News>>>>()
        val dbSearchResult = MutableLiveData<List<News>>()
        `when`(dao.search("foo")).thenReturn(dbSearchResult)

        repository.search("foo", "2024-02-16 16:12:35", "us", "en", 10, false)
            .observeForever(observer)

        verify(observer).onChanged(Resource.loading(null))
        verifyNoMoreInteractions(service)
        reset(observer)

        val dbResult = TestUtil.createNews(10, "a", "b", "c", "d")
        dbSearchResult.postValue(dbResult)

        verify(observer).onChanged(Resource.success(dbResult))
        verifyNoMoreInteractions(service)
    }

    @Test
    fun searchFromServer() {
        val observer = mock<Observer<Resource<List<News>>>>()
        val dbSearchResult = MutableLiveData<List<News>>()
        `when`(dao.search("foo")).thenReturn(dbSearchResult)

        val news = TestUtil.createNews(2, "a", "b", "c", "d")
        val apiResponse = NewsSearchResponse(2, news)

        val callLiveData = MutableLiveData<ApiResponse<NewsSearchResponse>>()
        `when`(service.search("foo", "2024-02-16 16:12:35", "us", "en", 10))
            .thenReturn(callLiveData)

        repository.search("foo", "2024-02-16 16:12:35", "us", "en", 10, true)
            .observeForever(observer)

        verify(observer).onChanged(Resource.loading(null))
        verifyNoMoreInteractions(service)
        reset(observer)

        dbSearchResult.postValue(null)
        verify(service).search("foo", "2024-02-16 16:12:35", "us", "en", 10)
        val updatedResult = MutableLiveData<List<News>>()
        `when`(dao.search("foo")).thenReturn(updatedResult)
        updatedResult.postValue(news)

        callLiveData.postValue(ApiResponse.create(Response.success(apiResponse)))
        verify(dao).insertNews(news)
        verify(observer).onChanged(Resource.success(news))
        verifyNoMoreInteractions(service)
    }

    @Test
    fun searchFromServerError() {
        `when`(dao.search("foo")).thenReturn(AbsentLiveData.create())
        val apiResponse = MutableLiveData<ApiResponse<NewsSearchResponse>>()
        `when`(service.search("foo", "2024-02-16 16:12:35", "us", "en", 10))
            .thenReturn(apiResponse)

        val observer = mock<Observer<Resource<List<News>>>>()
        repository.search("foo", "2024-02-16 16:12:35", "us", "en", 10, true)
            .observeForever(observer)
        verify(observer).onChanged(Resource.loading(null))

        apiResponse.postValue(ApiResponse.create(Exception("idk")))
        verify(observer).onChanged(Resource.error("idk", null))
    }

}