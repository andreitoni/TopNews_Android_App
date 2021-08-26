package com.example.assignment2

import com.example.assignment2.api.NewsApiJSON
import retrofit2.http.GET
import retrofit2.http.Query


interface APIRequest {


    @GET("/v2/top-headlines")
    suspend fun getNewsTopicsCountry(
        @Query("country") country: String,
        @Query("category") category: String,
        @Query("apiKey") apiKey: String
    ): NewsApiJSON


    @GET("/v2/top-headlines")
    suspend fun getNewsTopics(
        @Query("category") category: String,
        @Query("apiKey") apiKey: String,
        @Query("country") country:String
    ): NewsApiJSON

    @GET("/v2/top-headlines")
    suspend fun getSearchTopic(
        @Query("q") q: String,
        @Query("apiKey") apiKey: String
    ): NewsApiJSON


}