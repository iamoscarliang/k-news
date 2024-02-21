package com.oscarliang.knews.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oscarliang.knews.util.TestUtil
import com.oscarliang.knews.util.getOrAwaitValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewsDaoTest : NewsDatabaseTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun insertAndRead() {
        val news = TestUtil.createNewsWithId(1, "a", "b", "c", "d")
        db.newsDao().insertNews(listOf(news))
        val loaded = db.newsDao().getNewsById(1).getOrAwaitValue()
        assertNotNull(loaded)
        assertEquals(loaded.title, "a")
        assertEquals(loaded.image, "b")
        assertEquals(loaded.publishDate, "c")
        assertEquals(loaded.author, "d")
    }

    @Test(expected = AssertionError::class)
    fun insertBreakingNewsWithoutNews() {
        val breakingNews = TestUtil.createBreakingNews(2)
        db.newsDao().insertBreakingNews(breakingNews)
        throw AssertionError("Must fail because news does not exist")
    }

    @Test
    fun insertBreakingNews() {
        val news = TestUtil.createNewsWithIds(2, "a", "b", "c", "d")
        val breakingNews = TestUtil.createBreakingNews(2)
        db.runInTransaction {
            db.newsDao().insertNews(news)
            db.newsDao().insertBreakingNews(breakingNews)
        }

        val list = db.newsDao().getBreakingNews().getOrAwaitValue()
        assertEquals(list.size, 2)

        val first = list[0]
        assertEquals(first.id, 0)
        assertEquals(first.title, "a0")

        val second = list[1]
        assertEquals(second.id, 1)
        assertEquals(second.title, "a1")
    }

    @Test(expected = AssertionError::class)
    fun insertSearchResultWithoutNews() {
        val searchResults = TestUtil.createNewsSearchResults(2, "foo")
        db.newsDao().insertNewsSearchResults(searchResults)
        throw AssertionError("Must fail because news does not exist")
    }

    @Test
    fun insertSearchResult() {
        val news = TestUtil.createNewsWithIds(2, "a", "b", "c", "d")
        val searchResults = TestUtil.createNewsSearchResults(2, "foo")
        db.runInTransaction {
            db.newsDao().insertNews(news)
            db.newsDao().insertNewsSearchResults(searchResults)
        }

        val list = db.newsDao().search("foo").getOrAwaitValue()
        assertEquals(list.size, 2)

        val first = list[0]
        assertEquals(first.id, 0)
        assertEquals(first.title, "a0")

        val second = list[1]
        assertEquals(second.id, 1)
        assertEquals(second.title, "a1")
    }

}