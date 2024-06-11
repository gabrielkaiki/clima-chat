package com.gabrielkaiki.climaplicativo.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gabrielkaiki.climaplicativo.R
import com.gabrielkaiki.climaplicativo.databinding.ActivityAlterarSenhaBinding
import com.gabrielkaiki.climaplicativo.model.Usuario
import com.gabrielkaiki.climaplicativo.utils.*
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AlterarSenhaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlterarSenhaBinding
    private lateinit var novaSenha: TextInputEditText
    private lateinit var confirmarNovaSenha: TextInputEditText
    private lateinit var botaoSalvar: Button
    private lateinit var senha: String
    private var usuario: Usuario? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlterarSenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Toolbar
        configuracaoToolbar()

        //Inicializar componenetes
        inicializarComponentes()

        //Recuperar usuário
        recuperarUsuario()

        //Recuperar senha
        recuperarSenha()
    }

    private fun recuperarSenha() {
        val bundle = intent.extras
        if (bundle != null) {
            senha = bundle.getString("senha")!!
        }
    }

    private fun inicializarComponentes() {
        novaSenha = binding.inputNovaSenha
        confirmarNovaSenha = binding.inputConfirmarSenha
        botaoSalvar = binding.buttonSalvarSenha

        botaoSalvar.setOnClickListener {
            validarCampos()
        }
    }

    private fun validarCampos() {

        val novaSenha = novaSenha.text.toString()
        val confirmarNovaSenha = confirmarNovaSenha.text.toString()

            if (!novaSenha.isNullOrEmpty()) {
                if (!confirmarNovaSenha.isNullOrEmpty()) {
                    if (novaSenha == confirmarNovaSenha) {
                            salvarSenha(novaSenha)
                    } else {
                        Toast.makeText(
                            this,
                            "As senhas não são iguais. Verifique a nova senha e a confirmação.",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                } else {
                    Toast.makeText(this, "Por favor, confirme a nova senha!", Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                Toast.makeText(this, "Por favor, informe a nova senha!", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun salvarSenha(novaSenha: String) {
        var usuario = getUsuarioAtual()

        usuario?.updatePassword(novaSenha)?.addOnSuccessListener {

            //Salvar no fireBase
            var senhaCriptografada = encode(novaSenha)
            var usuarioRef =
                getDatabaseReference()!!.child("usuarios").child(getIdUsuarioLogado()!!)
                    .child("senha")

            usuarioRef.setValue(senhaCriptografada)

            Toast.makeText(
                this@AlterarSenhaActivity,
                "Senha atualizada com sucesso.",
                Toast.LENGTH_SHORT
            ).show()

        }?.addOnFailureListener {
            Toast.makeText(
                this@AlterarSenhaActivity,
                "Erro: ${it.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun configuracaoToolbar() {
        var toolbar = binding.includeToolbar.toolbar
        toolbar.title = "Alterar senha"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
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

    private fun recuperarUsuario() {
        var alertDialog = alertDialogPadrao(this)
        alertDialog.show()

        var usuarioRef = getDatabaseReference()!!.child("usuarios").child(getIdUsuarioLogado()!!)

        usuarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usuario = snapshot.getValue(Usuario::class.java)!!
                alertDialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                alertDialog.dismiss()
                Toast.makeText(
                    this@AlterarSenhaActivity,
                    "Erro: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    override fun onStart() {
        super.onStart()
        if (getFirebaseAuth()!!.currentUser == null) {
            finish()
        }
    }
}