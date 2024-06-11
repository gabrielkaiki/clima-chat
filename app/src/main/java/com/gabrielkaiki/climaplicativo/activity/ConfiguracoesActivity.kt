package com.gabrielkaiki.climaplicativo.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.gabrielkaiki.climaplicativo.R
import com.gabrielkaiki.climaplicativo.databinding.ActivityConfiguracoesBinding
import com.gabrielkaiki.climaplicativo.utils.getFirebaseAuth

class ConfiguracoesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Toolbar
        var toolbar = binding.includeToolbar.toolbar
        toolbar.title = "Configurações"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        var botaoDadosPessoais = binding.textDadosPessoais
        var botaoAlterarSenha = binding.textAlterarSenha
        var botaoExcluirConta = binding.textExcluirConta

        botaoDadosPessoais.setOnClickListener {
            startActivity(Intent(this, DadosPessoaisActivity::class.java))
        }

        botaoAlterarSenha.setOnClickListener {
            startActivity(Intent(this, LoginSegurancaActivity::class.java))
        }

        botaoExcluirConta.setOnClickListener {
            startActivity(Intent(this, ExcluirContaActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu!!.getItem(2).isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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

    override fun onStart() {
        super.onStart()
        if (getFirebaseAuth()!!.currentUser == null) {
            finish()
        }
    }
}