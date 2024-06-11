package com.gabrielkaiki.climaplicativo.api

import com.gabrielkaiki.climaplicativo.model.Distrito
import com.gabrielkaiki.climaplicativo.model.Estado
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LocalidadesServices {
    @GET("estados")
    fun getEstados(@Query("orderBy") ordenacao: String): Call<List<Estado>>

    @GET("estados/{uf}/municipios")
    fun getDistritos(
        @Path("uf") uf: String,
        @Query("orderBy") ordenacao: String
    ): Call<List<Distrito>>
}