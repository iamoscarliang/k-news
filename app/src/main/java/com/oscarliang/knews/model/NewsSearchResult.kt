package com.oscarliang.knews.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_search_results")
data class NewsSearchResult(
    @PrimaryKey
    val newsId: Int,
    val query: String,
    val position: Int
)
