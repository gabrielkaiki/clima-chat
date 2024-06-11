package com.gabrielkaiki.climaplicativo.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.gabrielkaiki.climaplicativo.R
import com.gabrielkaiki.climaplicativo.databinding.ActivityLoginSegurancaBinding
import com.gabrielkaiki.climaplicativo.utils.getFirebaseAuth
import com.gabrielkaiki.climaplicativo.utils.getUsuarioAtual
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider

class LoginSegurancaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginSegurancaBinding
    private lateinit var botaoValidar: Button
    private lateinit var email: TextInputEditText
    private lateinit var senha: TextInputEditText
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginSegurancaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Toolbar
        configurarToolbar()

        //Inicializar componentes
        inicializarComponentes()
    }

    private fun inicializarComponentes() {
        botaoValidar = binding.botaoAcessarLoginSeguranca
        email = binding.inputEmailLoginSeguranca
        senha = binding.inputSenhaLoginSeguranca
        progressBar = binding.progressBarLoginSeguranca

        botaoValidar.setOnClickListener {
            validarCampos()
        }
    }

    private fun validarCampos() {
        val campoEmail = email.text.toString()
        val campoSenha = senha.text.toString()

        if (campoEmail.isNotEmpty()) {
            if (campoSenha.isNotEmpty()) {
                reautenticar()
            } else {
                Toast.makeText(
                    this@LoginSegurancaActivity,
                    "Por favor, informe a sua senha!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this@LoginSegurancaActivity,
                "Por favor, informe o seu e-mail!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun reautenticar() {
        val usuario = getUsuarioAtual()!!

        val campoEmail = email.text.toString()
        val campoSenha = senha.text.toString()

        val dados = EmailAuthProvider.getCredential(campoEmail, campoSenha)

        usuario.reauthenticate(dados).addOnCompleteListener {
            if (it.isSuccessful) {
                var intent = Intent(this@LoginSegurancaActivity, AlterarSenhaActivity::class.java)
                intent.putExtra("senha", campoSenha)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this@LoginSegurancaActivity,
                    "Falha: ${it.exception!!.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun configurarToolbar() {
        val toolbar = binding.includeToolbar.toolbar
        toolbar.title = "Validação"

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
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