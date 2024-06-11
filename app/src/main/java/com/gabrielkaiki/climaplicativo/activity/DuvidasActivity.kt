package com.gabrielkaiki.climaplicativo.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.Menu
import android.view.MenuItem
import com.gabrielkaiki.climaplicativo.R
import com.gabrielkaiki.climaplicativo.databinding.ActivityDuvidasBinding
import com.gabrielkaiki.climaplicativo.utils.getFirebaseAuth

class DuvidasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDuvidasBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDuvidasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Toolbar
        configurarToolbar()

        val terceiroParagrafo = binding.textTerceiroParagrafo
        val textSpan = SpannableStringBuilder(
            "Ao receber a notificação, você pode informar os usuários interessados nas condições " +
                    "climáticas de sua cidade, tirando fotos do céu ao tocar no ícone        na tela de chat para mostrar o clima atual através " +
                    "de imagens.Exemplo na imagem à seguir."
        )

        val image = resources.getDrawable(R.drawable.ic_imagem, null)
        image.setBounds(0, 0, 54, 54)

        val imageSpan = ImageSpan(image, ImageSpan.ALIGN_BOTTOM)

        textSpan.setSpan(
            imageSpan,
            150,
            155,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        terceiroParagrafo.text = textSpan
    }

    private fun configurarToolbar() {
        var toolbar = binding.includeToolbar.toolbar
        toolbar.title = ""
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_configuracoes -> {
                val intent = Intent(this, ConfiguracoesActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_home -> {
                startActivity(Intent(this, MapsActivity::class.java))
            }

            R.id.menu_sair -> {
                getFirebaseAuth()!!.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}