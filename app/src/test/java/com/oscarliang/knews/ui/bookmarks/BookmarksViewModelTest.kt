package com.oscarliang.knews.ui.bookmarks

import com.oscarliang.knews.model.News
import com.oscarliang.knews.repository.NewsRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(JUnit4::class)
class BookmarksViewModelTest {

    private val repository = mock(NewsRepository::class.java)
    private lateinit var viewModel: BookmarksViewModel

    @Before
    fun init() {
        viewModel = BookmarksViewModel(repository)
    }

    @Test
    fun update() {
        val current = News(-1, "b", "c", "d", "e", "f", true)
        val updated = current.copy(bookmark = false)
        viewModel.toggleBookmark(current)
        verify(repository).updateNews(updated)
    }

}