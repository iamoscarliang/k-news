package com.oscarliang.knews.ui.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.oscarliang.knews.model.News
import com.oscarliang.knews.repository.NewsRepository
import com.oscarliang.knews.util.Resource
import com.oscarliang.knews.util.TestUtil
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock

@RunWith(JUnit4::class)
class SearchViewModelTest {

    @Rule
    @JvmField
    val instantExecutor = InstantTaskExecutorRule()

    private val repository = mock(NewsRepository::class.java)
    private lateinit var viewModel: SearchViewModel

    @Before
    fun init() {
        viewModel = SearchViewModel(repository)
    }

    @Test
    fun empty() {
        val result = mock<Observer<Resource<List<News>>>>()
        viewModel.searchResults.observeForever(result)
        viewModel.loadNextPage()
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun basic() {
        val result = mock<Observer<Resource<List<News>>>>()
        viewModel.searchResults.observeForever(result)
        viewModel.setQuery("a", "b", "c", "d", 10)
        verify(repository).search("a", "b", "c", "d", 10, true)
        verify(repository, never()).searchNextPage("a", "b", "c", "d", 10)
    }

    @Test
    fun noObserverNoQuery() {
        `when`(repository.searchNextPage("a", "b", "c", "d", 10))
            .thenReturn(mock())
        viewModel.setQuery("a", "b", "c", "d", 10)
        verify(repository, never()).search("a", "b", "c", "d", 10, true)
        // next page is user interaction and even if loading state is not observed, we query
        // would be better to avoid that if main search query is not observed
        viewModel.loadNextPage()
        verify(repository).searchNextPage("a", "b", "c", "d", 10)
    }

    @Test
    fun swap() {
        val nextPage = MutableLiveData<Resource<Boolean>?>()
        `when`(repository.searchNextPage("a", "b", "c", "d", 10))
            .thenReturn(nextPage)

        val result = mock<Observer<Resource<List<News>>>>()
        viewModel.searchResults.observeForever(result)
        verifyNoMoreInteractions(repository)
        viewModel.setQuery("a", "b", "c", "d", 10)
        verify(repository).search("a", "b", "c", "d", 10, true)
        viewModel.loadNextPage()

        viewModel.loadMoreStatus.observeForever(mock())
        verify(repository).searchNextPage("a", "b", "c", "d", 10)
        assertEquals(nextPage.hasActiveObservers(), true)
        viewModel.setQuery("e", "f", "g", "h", 10)
        assertEquals(nextPage.hasActiveObservers(), false)
        verify(repository).search("e", "f", "g", "h", 10, true)
        verify(repository, never()).searchNextPage("e", "f", "g", "h", 10)
    }

    @Test
    fun retry() {
        viewModel.retry()
        verifyNoMoreInteractions(repository)
        viewModel.setQuery("a", "b", "c", "d", 10)
        viewModel.retry()
        verifyNoMoreInteractions(repository)
        viewModel.searchResults.observeForever(mock())
        verify(repository).search("a", "b", "c", "d", 10, true)
        reset(repository)
        viewModel.retry()
        verify(repository).search("a", "b", "c", "d", 10, true)
    }

    @Test
    fun resetSameQuery() {
        viewModel.searchResults.observeForever(mock())
        viewModel.setQuery("a", "b", "c", "d", 10)
        verify(repository).search("a", "b", "c", "d", 10, true)
        reset(repository)
        viewModel.setQuery("a", "b", "c", "d", 10)
        verifyNoMoreInteractions(repository)
        viewModel.setQuery("e", "f", "g", "h", 10)
        verify(repository).search("e", "f", "g", "h", 10, true)
    }

    @Test
    fun update() {
        val current = TestUtil.createNews("a", "b", "c", "d")
        val updated = current.copy(bookmark = true)
        viewModel.toggleBookmark(current)
        verify(repository).updateNews(updated)
    }

}