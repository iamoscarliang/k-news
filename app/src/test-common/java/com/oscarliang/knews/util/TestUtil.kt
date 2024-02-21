package com.oscarliang.knews.util

import com.oscarliang.knews.model.BreakingNews
import com.oscarliang.knews.model.News
import com.oscarliang.knews.model.NewsSearchResult

private const val UNKNOWN_ID = -1

object TestUtil {

    fun createNews(
        count: Int,
        title: String,
        image: String,
        publishDate: String,
        author: String
    ): List<News> {
        return (0 until count).map {
            createNews(
                title = title + it,
                image = image + it,
                publishDate = publishDate + it,
                author = author + it,
            )
        }
    }

    fun createNews(
        title: String,
        image: String,
        publishDate: String,
        author: String
    ) = News(
        id = UNKNOWN_ID,
        title = title,
        text = "",
        image = image,
        publishDate = publishDate,
        author = author,
        bookmark = false
    )

    fun createNewsWithIds(
        count: Int,
        title: String,
        image: String,
        publishDate: String,
        author: String
    ): List<News> {
        return (0 until count).map {
            createNewsWithId(
                id = it,
                title = title + it,
                image = image + it,
                publishDate = publishDate + it,
                author = author + it,
            )
        }
    }

    fun createNewsWithId(
        id: Int,
        title: String,
        image: String,
        publishDate: String,
        author: String
    ) = News(
        id = id,
        title = title,
        text = "",
        image = image,
        publishDate = publishDate,
        author = author,
        bookmark = false
    )

    fun createBreakingNews(
        count: Int
    ): List<BreakingNews> {
        return (0 until count).map {
            BreakingNews(newsId = it)
        }
    }

    fun createNewsSearchResults(
        count: Int,
        query: String
    ): List<NewsSearchResult> {
        return (0 until count).map {
            NewsSearchResult(
                newsId = it,
                query = query,
                position = it
            )
        }
    }

}