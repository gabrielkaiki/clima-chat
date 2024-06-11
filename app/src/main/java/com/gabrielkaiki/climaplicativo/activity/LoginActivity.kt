package com.gabrielkaiki.climaplicativo.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.gabrielkaiki.climaplicativo.databinding.ActivityLoginBinding
import com.gabrielkaiki.climaplicativo.model.Usuario
import com.gabrielkaiki.climaplicativo.utils.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var botaoLogin: Button
    private lateinit var botaoCadastrar: TextView
    private lateinit var textoCabecalho: TextView
    private lateinit var botaoEsqueciSenha: TextView
    private lateinit var campoEmail: TextInputEditText
    private lateinit var campoSenha: TextInputEditText
    private lateinit var usuario: Usuario
    private lateinit var progressBar: ProgressBar
    private lateinit var adView1: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Toolbar
        configurarToolbar()

        //Componentes
        inicializarComponentes()
    }

    private fun inicializarComponentes() {
        botaoLogin = binding.botaoAcessarLogin
        botaoCadastrar = binding.botaoAcessarCadastro
        botaoEsqueciSenha = binding.textEsqueciSenha
        campoEmail = binding.inputEmailLogin
        campoSenha = binding.inputSenhaLogin
        progressBar = binding.progressBarLogin
        textoCabecalho = binding.textCabecalho

        textoCabecalho.text =
            "Para acessar as mensagens enviadas por usuários que moram na região de ${cidadeAtual}, você deve " +
                    "se cadastrar no sistema. Se já possui um cadastro, apenas faça login."

        botaoLogin.setOnClickListener {
            logar()
        }

        botaoCadastrar.setOnClickListener {
            startActivity(Intent(this@LoginActivity, CadastroActivity::class.java))
            finish()
        }

        botaoEsqueciSenha.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RecuperarSenhaActivity::class.java))
            finish()
        }
    }

    private fun logar() {
        if (validarCampos()) {
            var email = campoEmail.text.toString()
            var senha = campoSenha.text.toString()

            getFirebaseAuth()!!.signInWithEmailAndPassword(email, senha).addOnSuccessListener {
                recuperarUsuario()
            }.addOnFailureListener {
                Toast.makeText(this@LoginActivity, "Erro: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun recuperarUsuario() {
        progressBar.visibility = View.VISIBLE
        var usuarioRef = getDatabaseReference()!!.child("usuarios").child(getIdUsuarioLogado()!!)
        usuarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    usuario = snapshot.getValue(Usuario::class.java)!!
                    var intent = Intent(this@LoginActivity, ChatActivity::class.java)
                    intent.putExtra("usuario", usuario)
                    startActivity(intent)

                    finish()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Esse usuário não existe no sistema.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginActivity, "Erro: ${error.message}", Toast.LENGTH_SHORT)
                    .show()
                progressBar.visibility = View.GONE
            }

        })
    }

    private fun validarCampos(): Boolean {
        var email = campoEmail.text.toString()
        var senha = campoSenha.text.toString()

        if (!email.isNullOrEmpty()) {
            if (!senha.isNullOrEmpty()) {
                return true
            } else {
                Toast.makeText(this, "Por favor, informe a sua senha!", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Por favor, informe o seu e-mail!", Toast.LENGTH_LONG).show()
        }
        return false
    }

    override fun onStart() {
        super.onStart()
        carregarAnuncios()
        if (getUsuarioAtual() != null) {
            recuperarUsuario()
        }
    }

    private fun carregarAnuncios() {
        adView1 = binding.adViewLogin
        val adRequest = AdRequest.Builder().build()
        adView1.loadAd(adRequest)
    }

    private fun configurarToolbar() {
        val toolbar = binding.includeToolbar.toolbar
        toolbar.title = "Acessar"

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}
