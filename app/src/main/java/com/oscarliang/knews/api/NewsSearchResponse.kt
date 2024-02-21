package com.oscarliang.knews.api

import com.google.gson.annotations.SerializedName
import com.oscarliang.knews.model.News

data class NewsSearchResponse(
    @SerializedName("number")
    val number: Int = 0,
    @SerializedName("news")
    val news: List<News>
)