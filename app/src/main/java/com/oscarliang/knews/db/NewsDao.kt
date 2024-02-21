package com.oscarliang.knews.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oscarliang.knews.model.BreakingNews
import com.oscarliang.knews.model.News
import com.oscarliang.knews.model.NewsSearchResult

@Dao
abstract class NewsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertNews(news: List<News>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertBreakingNews(breakingNews: List<BreakingNews>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertNewsSearchResults(newsSearchResult: List<NewsSearchResult>)

    @Query("SELECT * FROM breaking_news INNER JOIN news ON newsId = id")
    abstract fun getBreakingNews(): LiveData<List<News>>

    @Query("SELECT * FROM news_search_results INNER JOIN news ON newsId = id WHERE `query` = :query ORDER BY position")
    abstract fun search(query: String): LiveData<List<News>>

    @Query("SELECT COUNT(*) FROM news_search_results WHERE `query` = :query")
    abstract fun getSearchResultCount(query: String): Int

    @Query("SELECT * FROM news WHERE bookmark = 1")
    abstract fun getBookmarks(): LiveData<List<News>>

    @Query("SELECT * FROM news WHERE id = :id")
    abstract fun getNewsById(id: Int): LiveData<News>

    @Update
    abstract fun updateNews(news: News)

    @Query("DELETE FROM breaking_news")
    abstract fun deleteAllBreakingNews()

    @Query("DELETE FROM news_search_results WHERE `query` = :query")
    abstract fun deleteAllNewsSearchResults(query: String)

}
