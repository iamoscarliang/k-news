package com.oscarliang.knews.ui.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.oscarliang.knews.repository.NewsRepository
import com.oscarliang.knews.util.Resource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`

class NextPageHandlerTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repository = mock(NewsRepository::class.java)

    private lateinit var pageHandler: SearchViewModel.NextPageHandler

    @Before
    fun init() {
        pageHandler = SearchViewModel.NextPageHandler(repository)
    }

    private val status: SearchViewModel.LoadMoreState?
        get() = pageHandler.loadMoreState.value

    @Test
    fun constructor() {
        val initial = status
        assertNotNull(initial)
        assertEquals(initial?.isRunning, false)
        assertNull(initial?.errorMessage)
    }

    @Test
    fun reloadSameValue() {
        enqueueResponse("a", "b", "c", "d", 10)
        pageHandler.queryNextPage("a", "b", "c", "d", 10)
        verify(repository).searchNextPage("a", "b", "c", "d", 10)

        reset(repository)
        pageHandler.queryNextPage("a", "b", "c", "d", 10)
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun success() {
        val liveData = enqueueResponse("a", "b", "c", "d", 10)

        pageHandler.queryNextPage("a", "b", "c", "d", 10)
        verify(repository).searchNextPage("a", "b", "c", "d", 10)
        assertEquals(liveData.hasActiveObservers(), true)
        pageHandler.onChanged(Resource.loading(null))
        assertEquals(liveData.hasActiveObservers(), true)
        assertEquals(status?.isRunning, true)

        pageHandler.onChanged(Resource.success(true))
        assertEquals(liveData.hasActiveObservers(), false)
        assertEquals(pageHandler.hasMore, true)
        assertEquals(status?.isRunning, false)
        assertEquals(liveData.hasActiveObservers(), false)

        // requery
        reset(repository)
        val nextPage = enqueueResponse("a", "b", "c", "d", 10)
        pageHandler.queryNextPage("a", "b", "c", "d", 10)
        verify(repository).searchNextPage("a", "b", "c", "d", 10)
        assertEquals(nextPage.hasActiveObservers(), true)

        pageHandler.onChanged(Resource.success(false))
        assertEquals(liveData.hasActiveObservers(), false)
        assertEquals(pageHandler.hasMore, false)
        assertEquals(status?.isRunning, false)
        assertEquals(nextPage.hasActiveObservers(), false)

        // retry, no query
        reset(repository)
        pageHandler.queryNextPage("a", "b", "c", "d", 10)
        verifyNoMoreInteractions(repository)
        pageHandler.queryNextPage("a", "b", "c", "d", 10)
        verifyNoMoreInteractions(repository)

        // query another
        val bar = enqueueResponse("e", "f", "g", "h", 10)
        pageHandler.queryNextPage("e", "f", "g", "h", 10)
        verify(repository).searchNextPage("e", "f", "g", "h", 10)
        assertEquals(bar.hasActiveObservers(), true)
    }

    @Test
    fun failure() {
        val liveData = enqueueResponse("a", "b", "c", "d", 10)
        pageHandler.queryNextPage("a", "b", "c", "d", 10)
        assertEquals(liveData.hasActiveObservers(), true)
        pageHandler.onChanged(Resource.error("idk", false))
        assertEquals(liveData.hasActiveObservers(), false)
        assertEquals(status?.errorMessage, "idk")
        assertEquals(status?.errorMessageIfNotHandled, "idk")
        assertNull(status?.errorMessageIfNotHandled)
        assertEquals(status?.isRunning, false)
        assertEquals(pageHandler.hasMore, true)

        reset(repository)
        val liveData2 = enqueueResponse("a", "b", "c", "d", 10)
        pageHandler.queryNextPage("a", "b", "c", "d", 10)
        assertEquals(liveData2.hasActiveObservers(), true)
        assertEquals(status?.isRunning, true)
        pageHandler.onChanged(Resource.success(false))
        assertEquals(status?.isRunning, false)
        assertNull(status?.errorMessage)
        assertEquals(pageHandler.hasMore, false)
    }

    @Test
    fun nullOnChanged() {
        val liveData = enqueueResponse("a", "b", "c", "d", 10)
        pageHandler.queryNextPage("a", "b", "c", "d", 10)
        assertEquals(liveData.hasActiveObservers(), true)
        pageHandler.onChanged(null)
        assertEquals(liveData.hasActiveObservers(), false)
    }

    private fun enqueueResponse(
        query: String,
        date: String,
        country: String,
        language: String,
        number: Int
    ): MutableLiveData<Resource<Boolean>?> {
        val liveData = MutableLiveData<Resource<Boolean>?>()
        `when`(repository.searchNextPage(query, date, country, language, number))
            .thenReturn(liveData)
        return liveData
    }

}