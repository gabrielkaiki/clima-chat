package com.gabrielkaiki.climaplicativo.utils

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun getRetrofit(): Retrofit {
    var retrofit = Retrofit.Builder()
        .baseUrl("https://api.hgbrasil.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    return retrofit
}

fun getRetrofitLocalidades(): Retrofit {
    var retrofit = Retrofit.Builder()
        .baseUrl("https://servicodados.ibge.gov.br/api/v1/localidades/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    return retrofit
}

fun getRetrofitNotificacao(): Retrofit {
    var retrofit = Retrofit.Builder()
        .baseUrl("https://fcm.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    return retrofit
}