package com.gabrielkaiki.climaplicativo.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.gabrielkaiki.climaplicativo.databinding.ActivityRecuperarSenhaBinding
import com.gabrielkaiki.climaplicativo.utils.getFirebaseAuth
import com.gabrielkaiki.climaplicativo.utils.getUsuarioAtual
import com.google.android.material.textfield.TextInputEditText

class RecuperarSenhaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecuperarSenhaBinding
    private lateinit var botaoEnviarEmail: Button
    private lateinit var inputEmail: TextInputEditText
    private lateinit var textMensagemRedefinicaoSenha: TextView
    private var usuario = getUsuarioAtual()
    private var autenticacao = getFirebaseAuth()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecuperarSenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Toolbar
        configurarToolbar()

        //Inicializar componentes
        inicializarComponentes()
    }

    private fun inicializarComponentes() {
        botaoEnviarEmail = binding.buttonEnviarEmailRecuperacao
        inputEmail = binding.inputEmailRecuperacao
        textMensagemRedefinicaoSenha = binding.textMensagemRedefinicaoSenha

        botaoEnviarEmail.setOnClickListener {
            var email = inputEmail.text.toString()

            if (!email.isNullOrEmpty()) {
                autenticacao!!.sendPasswordResetEmail(email).addOnSuccessListener {
                    textMensagemRedefinicaoSenha.visibility = View.VISIBLE
                }.addOnFailureListener {
                    Toast.makeText(
                        this@RecuperarSenhaActivity,
                        "Erro: ${it.message}.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "Por favor, informe um e-mail!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarToolbar() {
        var toolbar = binding.includeToolbar.toolbar
        toolbar.title = "Recuperar senha"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}