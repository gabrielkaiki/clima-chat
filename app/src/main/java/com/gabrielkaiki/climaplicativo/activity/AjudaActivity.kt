package com.gabrielkaiki.climaplicativo.activity

import android.os.Bundle
import com.gabrielkaiki.climaplicativo.R
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide

class AjudaActivity : IntroActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_ajuda)

        isButtonBackVisible = false
        isButtonNextVisible = false

        addSlide(
            FragmentSlide.Builder()
                .background(R.color.cor_oceano_mapa)
                .fragment(R.layout.intro_1)
                .build()
        )

        addSlide(
            FragmentSlide.Builder()
                .background(R.color.cor_oceano_mapa)
                .fragment(R.layout.intro_2)
                .build()
        )

        addSlide(
            FragmentSlide.Builder()
                .background(R.color.cor_oceano_mapa)
                .fragment(R.layout.intro_3)
                .build()
        )

        addSlide(
            FragmentSlide.Builder()
                .background(R.color.cor_teclado)
                .fragment(R.layout.intro_4)
                .build()
        )
    }
}