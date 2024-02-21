package com.oscarliang.knews.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breaking_news")
data class BreakingNews(
    @PrimaryKey
    val newsId: Int
)