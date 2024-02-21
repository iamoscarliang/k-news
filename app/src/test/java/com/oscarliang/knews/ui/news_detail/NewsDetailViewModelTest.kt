package com.oscarliang.knews.ui.news_detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.oscarliang.knews.model.News
import com.oscarliang.knews.repository.NewsRepository
import com.oscarliang.knews.ui.newsdetail.NewsDetailViewModel
import com.oscarliang.knews.util.TestUtil
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

class NewsDetailViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repository = mock(NewsRepository::class.java)
    private lateinit var viewModel: NewsDetailViewModel

    @Before
    fun init() {
        viewModel = NewsDetailViewModel(repository)
    }

    @Test
    fun testNull() {
        assertNotNull(viewModel.news)
        verify(repository, never()).getNewsById(anyInt())
        viewModel.setNewsId(1)
        verify(repository, never()).getNewsById(anyInt())
    }

    @Test
    fun testCallNews() {
        viewModel.news.observeForever(mock())
        viewModel.setNewsId(1)
        verify(repository).getNewsById(1)
        reset(repository)
        viewModel.setNewsId(2)
        verify(repository).getNewsById(2)
    }

    @Test
    fun sendResultToUI() {
        val foo = MutableLiveData<News>()
        val bar = MutableLiveData<News>()
        `when`(repository.getNewsById(1)).thenReturn(foo)
        `when`(repository.getNewsById(2)).thenReturn(bar)
        val observer = mock<Observer<News>>()
        viewModel.news.observeForever(observer)
        viewModel.setNewsId(1)
        verify(observer, never()).onChanged(any())
        val fooNews = TestUtil.createNews("foo", "a", "b", "c")

        foo.value = fooNews
        verify(observer).onChanged(fooNews)
        reset(observer)
        val barNews = TestUtil.createNews("bar", "a", "b", "c")
        bar.value = barNews
        viewModel.setNewsId(2)
        verify(observer).onChanged(barNews)
    }

    @Test
    fun dontRefreshOnSameId() {
        val observer = mock<Observer<Int?>>()
        viewModel.id.observeForever(observer)
        verifyNoMoreInteractions(observer)
        viewModel.setNewsId(1)
        verify(observer).onChanged(1)
        reset(observer)
        viewModel.setNewsId(1)
        verifyNoMoreInteractions(observer)
        viewModel.setNewsId(2)
        verify(observer).onChanged(2)
    }

    @Test
    fun nullId() {
        viewModel.setNewsId(1)
        viewModel.setNewsId(null)
        val observer = mock<Observer<News?>>()
        viewModel.news.observeForever(observer)
        verify(observer).onChanged(null)
    }

    @Test
    fun update() {
        val current = TestUtil.createNews("a", "b", "c", "d")
        val updated = current.copy(bookmark = true)
        viewModel.toggleBookmark(current)
        verify(repository).updateNews(updated)
    }

}