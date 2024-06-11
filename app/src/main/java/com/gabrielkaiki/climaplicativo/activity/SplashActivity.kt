package com.gabrielkaiki.climaplicativo.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.gabrielkaiki.climaplicativo.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            abrirTelaPrincipal()
            finish()
        }, 500)

    }

    fun abrirTelaPrincipal() {

        //Intent
        var intentMaps = Intent(this, MapsActivity::class.java)

        //Bundle
        var bundle = intent.extras
        if (bundle != null) {
            intentMaps.putExtra("notificação", true)
        }
        startActivity(intentMaps)
    }
}