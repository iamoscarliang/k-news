package com.oscarliang.knews.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.oscarliang.knews.model.BreakingNews
import com.oscarliang.knews.model.News
import com.oscarliang.knews.model.NewsSearchResult

@Database(
    entities = [News::class, BreakingNews::class, NewsSearchResult::class],
    version = 1
)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun newsDao(): NewsDao

}