package com.oscarliang.knews.api

import androidx.lifecycle.LiveData
import com.oscarliang.knews.util.API_KEY
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NewsService {

    @Headers("x-api-key: $API_KEY")
    @GET("search-news")
    fun getBreakingNews(
        @Query("earliest-publish-date") date: String,
        @Query("source-countries") country: String,
        @Query("language") language: String,
        @Query("number") number: Int
    ): LiveData<ApiResponse<NewsSearchResponse>>

    @Headers("x-api-key: $API_KEY")
    @GET("search-news")
    fun search(
        @Query("text") query: String,
        @Query("earliest-publish-date") date: String,
        @Query("source-countries") country: String,
        @Query("language") language: String,
        @Query("number") number: Int
    ): LiveData<ApiResponse<NewsSearchResponse>>

    @Headers("x-api-key: $API_KEY")
    @GET("search-news")
    fun search(
        @Query("text") query: String,
        @Query("earliest-publish-date") date: String,
        @Query("source-countries") country: String,
        @Query("language") language: String,
        @Query("number") number: Int,
        @Query("offset") offset: Int
    ): Call<NewsSearchResponse>

}
