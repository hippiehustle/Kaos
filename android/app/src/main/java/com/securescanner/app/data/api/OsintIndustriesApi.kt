package com.securescanner.app.data.api

import com.securescanner.app.data.model.OsintIndustriesCredits
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface OsintIndustriesApi {

    @GET("v2/request")
    suspend fun search(
        @Header("api-key") apiKey: String,
        @Query("type") type: String, // "email" or "phone"
        @Query("query") query: String
    ): Response<JsonObject>

    @GET("misc/credits")
    suspend fun getCredits(
        @Header("api-key") apiKey: String
    ): OsintIndustriesCredits
}
