package com.gabrielkaiki.climaplicativo.api

import com.gabrielkaiki.climaplicativo.model.NotificacaoBase
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificacaoService {

    @Headers(
        "X-RapidAPI-Key': 'e11868a319msh817156adfdb5489p1819fcjsne24162f60ce8",
        "X-RapidAPI-Host': 't-one-youtube-converter.p.rapidapi.com"
    )
    @POST("fcm/send")
    fun enviar(@Body notificacaoDados: NotificacaoBase?): Call<NotificacaoBase?>?
}