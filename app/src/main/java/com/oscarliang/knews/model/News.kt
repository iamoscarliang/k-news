package com.oscarliang.knews.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "news")
data class News(
    @PrimaryKey
    @field:SerializedName("id")
    val id: Int,
    @field:SerializedName("title")
    val title: String,
    @field:SerializedName("text")
    val text: String,
    @field:SerializedName("image")
    val image: String?,
    @field:SerializedName("publish_date")
    val publishDate: String?,
    @field:SerializedName("author")
    val author: String?,
    val bookmark: Boolean = false
)
