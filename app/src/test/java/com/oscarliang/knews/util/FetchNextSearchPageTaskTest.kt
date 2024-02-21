package com.oscarliang.knews.util

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.oscarliang.knews.api.NewsSearchResponse
import com.oscarliang.knews.api.NewsService
import com.oscarliang.knews.db.NewsDao
import com.oscarliang.knews.db.NewsDatabase
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
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
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

@RunWith(JUnit4::class)
class FetchNextSearchPageTaskTest {

    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: NewsService

    private lateinit var db: NewsDatabase

    private lateinit var newsDao: NewsDao

    private lateinit var task: FetchNextSearchPageTask

    private val observer: Observer<Resource<Boolean>?> = mock()

    @Before
    fun init() {
        service = mock(NewsService::class.java)
        db = mock(NewsDatabase::class.java)
        `when`(db.runInTransaction(any())).thenCallRealMethod()
        newsDao = mock(NewsDao::class.java)
        `when`(db.newsDao()).thenReturn(newsDao)
        task = FetchNextSearchPageTask(
            query = "foo",
            date = "2024-02-16 16:12:35",
            country = "us",
            language = "en",
            number = 10,
            newsService = service,
            db = db
        )
        task.liveData.observeForever(observer)
    }

    @Test
    fun withoutResult() {
        createDbResult(0)
        task.run()
        verify(observer).onChanged(null)
        verifyNoMoreInteractions(observer)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun noNextPage() {
        createDbResult(3)
        task.run()
        verify(observer).onChanged(Resource.success(false))
        verifyNoMoreInteractions(observer)
        verifyNoMoreInteractions(service)

        reset(observer)
        createDbResult(13)
        task.run()
        verify(observer).onChanged(Resource.success(false))
        verifyNoMoreInteractions(observer)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun nextPageWithNull() {
        createDbResult(10)
        val news = TestUtil.createNews(3, "a", "b", "c", "d")
        val result = NewsSearchResponse(3, news)
        val call = createCall(result)
        `when`(service.search("foo", "2024-02-16 16:12:35", "us", "en", 10, 10))
            .thenReturn(call)
        task.run()
        verify(newsDao).insertNews(news)
        verify(observer).onChanged(Resource.success(false))
    }

    @Test
    fun nextPageWithMore() {
        createDbResult(10)
        val news = TestUtil.createNews(10, "a", "b", "c", "d")
        val result = NewsSearchResponse(10, news)
        val call = createCall(result)
        `when`(service.search("foo", "2024-02-16 16:12:35", "us", "en", 10, 10))
            .thenReturn(call)
        task.run()
        verify(newsDao).insertNews(news)
        verify(observer).onChanged(Resource.success(true))
    }

    @Test
    fun nextPageApiError() {
        createDbResult(10)
        val call = mock<Call<NewsSearchResponse>>()
        `when`(call.execute()).thenReturn(
            Response.error(
                400,
                "bar".toResponseBody("txt".toMediaTypeOrNull())
            )
        )
        `when`(service.search("foo", "2024-02-16 16:12:35", "us", "en", 10, 10))
            .thenReturn(call)
        task.run()
        verify(observer)!!.onChanged(Resource.error("bar", true))
    }

    @Test
    fun nextPageIOError() {
        createDbResult(10)
        val call = mock<Call<NewsSearchResponse>>()
        `when`(call.execute()).thenThrow(IOException("bar"))
        `when`(service.search("foo", "2024-02-16 16:12:35", "us", "en", 10, 10))
            .thenReturn(call)
        task.run()
        verify(observer)!!.onChanged(Resource.error("bar", true))
    }

    private fun createDbResult(count: Int?) {
        `when`(newsDao.getSearchResultCount("foo")).thenReturn(count)
    }

    private fun createCall(body: NewsSearchResponse): Call<NewsSearchResponse> {
        val success = Response.success(body)
        val call = mock<Call<NewsSearchResponse>>()
        `when`(call.execute()).thenReturn(success)
        return call
    }

}