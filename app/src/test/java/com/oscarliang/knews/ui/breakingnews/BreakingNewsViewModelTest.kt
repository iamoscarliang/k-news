package com.oscarliang.knews.ui.breakingnews

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.oscarliang.knews.model.News
import com.oscarliang.knews.repository.NewsRepository
import com.oscarliang.knews.util.Resource
import com.oscarliang.knews.util.TestUtil
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.mock

@RunWith(JUnit4::class)
class BreakingNewsViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repository = mock(NewsRepository::class.java)
    private lateinit var viewModel: BreakingNewsViewModel

    @Before
    fun init() {
        viewModel = BreakingNewsViewModel(repository)
    }

    @Test
    fun testNull() {
        assertNotNull(viewModel.news)
        verify(repository, never()).getBreakingNews(
            anyString(),
            anyString(),
            anyString(),
            anyInt(),
            anyBoolean()
        )
    }

    @Test
    fun dontFetchWithoutObservers() {
        viewModel.setQuery("a", "b", "c", 10)
        verify(repository, never()).getBreakingNews(
            anyString(),
            anyString(),
            anyString(),
            anyInt(),
            anyBoolean()
        )
    }

    @Test
    fun fetchWhenObserved() {
        viewModel.setQuery("a", "b", "c", 10)
        viewModel.news.observeForever(mock())
        verify(repository).getBreakingNews("a", "b", "c", 10, false)
    }

    @Test
    fun changeWhileObserved() {
        viewModel.news.observeForever(mock())

        viewModel.setQuery("a", "b", "c", 10)
        viewModel.setQuery("d", "e", "f", 10)

        verify(repository).getBreakingNews("a", "b", "c", 10, false)
        verify(repository).getBreakingNews("d", "e", "f", 10, false)
    }

    @Test
    fun resetQuery() {
        val observer = mock<Observer<BreakingNewsViewModel.Query>>()
        viewModel.query.observeForever(observer)
        verifyNoMoreInteractions(observer)
        viewModel.setQuery("a", "b", "c", 10)
        verify(observer).onChanged(BreakingNewsViewModel.Query("a", "b", "c", 10, false))
        reset(observer)
        viewModel.setQuery("a", "b", "c", 10)
        verifyNoMoreInteractions(observer)
        viewModel.setQuery("d", "e", "f", 10)
        verify(observer).onChanged(BreakingNewsViewModel.Query("d", "e", "f", 10, false))
    }

    @Test
    fun refresh() {
        viewModel.refresh()
        verifyNoMoreInteractions(repository)
        viewModel.setQuery("a", "b", "c", 10)
        verifyNoMoreInteractions(repository)
        val observer = mock<Observer<Resource<List<News>>>>()
        viewModel.news.observeForever(observer)
        verify(repository).getBreakingNews("a", "b", "c", 10, false)
        reset(repository)
        viewModel.refresh()
        verify(repository).getBreakingNews("a", "b", "c", 10, true)
    }

    @Test
    fun blankQuery() {
        viewModel.setQuery("", "", "", 10)
        val observer = mock<Observer<Resource<List<News>>?>>()
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