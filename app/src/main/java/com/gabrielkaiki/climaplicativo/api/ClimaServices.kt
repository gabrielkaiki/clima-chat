package com.gabrielkaiki.climaplicativo.api

import com.gabrielkaiki.climaplicativo.model.Clima
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ClimaServices {

    @GET("weather")
    fun getClima(
        @Query("key") key: String,
        @Query("format") formato: String,
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("user_ip") userIp: String
    ): Call<Clima>

    @GET("weather")
    fun getClimaCidade(
        @Query("key") key: String,
        @Query("format") formato: String,
        @Query("city_name") cidade: String,
    ): Call<Clima>
}