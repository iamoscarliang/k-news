package com.oscarliang.knews.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.oscarliang.knews.util.LiveDataCallAdapterFactory
import com.oscarliang.knews.util.getOrAwaitValue
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(JUnit4::class)
class NewsServiceTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: NewsService

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
            .create(NewsService::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun getBreakingNews() {
        enqueueResponse("breaking-news.json")
        val response = service.getBreakingNews(
            "2024-02-16 16:12:35",
            "us",
            "en",
            2
        ).getOrAwaitValue() as ApiSuccessResponse

        val request = mockWebServer.takeRequest()
        assertEquals(
            request.path,
            "/search-news?earliest-publish-date=2024-02-16%2016%3A12%3A35&source-countries=us&language=en&number=2"
        )

        assertNotNull(response)
        assertEquals(response.body.number, 2)
        assertEquals(response.body.news.size, 2)

        val news1 = response.body.news.get(0)
        assertEquals(news1.id, 190812741)
        assertEquals(
            news1.title,
            "Alaska Woman Gets 99 Years in Catfish Murder-for-Hire of Best Friend"
        )
        assertEquals(
            news1.image,
            "https://static01.nyt.com/images/2024/02/15/multimedia/15xp-alaska-ljtq/15xp-alaska-ljtq-facebookJumbo-v2.jpg"
        )
        assertEquals(news1.author, "Jesus Jiménez")
        assertEquals(news1.publishDate, "2024-02-16 04:15:24")


        val news2 = response.body.news.get(1)
        assertEquals(news2.id, 190811827)
    }

    @Test
    fun search() {
        enqueueResponse("search.json")
        val response = service.search(
            "foo",
            "2024-02-16 16:12:35",
            "us",
            "en",
            10
        ).getOrAwaitValue() as ApiSuccessResponse

        val request = mockWebServer.takeRequest()
        assertEquals(
            request.path,
            "/search-news?text=foo&earliest-publish-date=2024-02-16%2016%3A12%3A35&source-countries=us&language=en&number=10"
        )

        assertNotNull(response)
        assertEquals(response.body.number, 10)
        assertEquals(response.body.news.size, 10)

        val news1 = response.body.news.get(0)
        assertEquals(news1.id, 190809371)

        val news2 = response.body.news.get(1)
        assertEquals(news2.id, 190936711)
    }

    private fun enqueueResponse(fileName: String, headers: Map<String, String> = emptyMap()) {
        val inputStream = javaClass.classLoader!!
            .getResourceAsStream("api-response/$fileName")
        val source = inputStream.source().buffer()
        val mockResponse = MockResponse()
        for ((key, value) in headers) {
            mockResponse.addHeader(key, value)
        }
        mockWebServer.enqueue(
            mockResponse
                .setBody(source.readString(Charsets.UTF_8))
        )
    }

}