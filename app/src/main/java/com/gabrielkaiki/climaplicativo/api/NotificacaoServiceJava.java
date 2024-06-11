package com.gabrielkaiki.climaplicativo.api;

import com.gabrielkaiki.climaplicativo.model.NotificacaoBase;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificacaoServiceJava {

    @Headers({
            "Authorization: Key=AAAA0_9FpPI:APA91bEZeSzOsiDHRP4k4HgSyu2ZMhvZt-hej2Wr62g3NBPDr7N9ZNtW7Gv2MmTrswz_1jxq7HfdAK4EKuaDDpd3LYBv3a2HVATe_JWo54JJyv5eP8a96SGyPmm4l9U_un7xbL5eSY1j",
            "Content-Type:application/json"})
    @POST("fcm/send")
    Call<NotificacaoBase> enviar(@Body NotificacaoBase notificacaoBase);

}
