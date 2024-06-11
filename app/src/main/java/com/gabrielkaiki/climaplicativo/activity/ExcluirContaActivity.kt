package com.gabrielkaiki.climaplicativo.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.gabrielkaiki.climaplicativo.R
import com.gabrielkaiki.climaplicativo.databinding.ActivityExcluirContaBinding
import com.gabrielkaiki.climaplicativo.model.Usuario
import com.gabrielkaiki.climaplicativo.utils.*
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage

class ExcluirContaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExcluirContaBinding
    private lateinit var botaoExcluir: Button
    private lateinit var textoMotivoExclusaoConta: EditText
    private var usuarioLogado: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExcluirContaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Toolbar
        configurarToolbar()

        //Recuperar usuário
        recuperarUsuario()

        //Componentes
        textoMotivoExclusaoConta = binding.editTextMotivo
        botaoExcluir = binding.buttonExcluir

        //Botão de excluir usuário listener
        botaoExcluir.setOnClickListener {
            val idUser = getIdUsuarioLogado()
            removerMensagensEImagens(idUser)
        }
    }

    private fun removerMensagensEImagens(idUser: String?) {
        //Remover mensagens
        val msgRef = getDatabaseReference()!!.child("referencias_mensagens_usuario").child(
            idUser!!
        )

        msgRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChildren()) {
                    val map = HashMap<String, Any?>()
                    for (referencia in snapshot.children) {
                        val referenciaMensagemImagem = referencia.getValue(String::class.java)!!
                        map[referenciaMensagemImagem] = null
                    }
                    getDatabaseReference()!!.updateChildren(map)
                    msgRef.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        //Remover imagens do storage
        val refImagensStorage =
            getDatabaseReference()!!.child("referencias_imagensStorage_usuario").child(
                idUser
            )

        refImagensStorage.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChildren()) {
                    for (referencia in snapshot.children) {
                        val ref = referencia.getValue(String::class.java)
                        val refImg = FirebaseStorage.getInstance().getReferenceFromUrl(ref!!)
                        refImg.delete()
                    }
                    refImagensStorage.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        //Remover usuário do banco de dados
        val usuarioRef =
            getDatabaseReference()!!.child("usuarios").child(idUser)
        usuarioRef.removeValue()

        //Remoção da inscrição em tópico do firebase messaging
        val cidadeFormatada = removerAcentosEEspacos(usuarioLogado!!.cidade!!)
        FirebaseMessaging.getInstance()
            .unsubscribeFromTopic(cidadeFormatada)

        //Decrementar usuário de cidade
        decrementarCidade(usuarioLogado?.cidade!!)

        //Se houver um motivo da exclusão, salvar no banco de dados
        if (!textoMotivoExclusaoConta.text.isNullOrEmpty())
            salvarMotivoExclusao(idUser)

        //Remover último acesso
        val ultimoAcessoRef = getDatabaseReference()!!.child("ultimoAcesso").child(idUser)
        ultimoAcessoRef.removeValue()

        //Deletar autenticação do usuário
        deletarUsuario()
    }

    private fun deletarUsuario() {
        val usuario = getUsuarioAtual()
        usuario?.reauthenticate(authCredential)?.addOnCompleteListener {
            if (it.isSuccessful) {
                usuario.delete().addOnCompleteListener {
                    if (it.isSuccessful) {
                        getFirebaseAuth()!!.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                    } else {
                        Toast.makeText(
                            this@ExcluirContaActivity,
                            "Erro ao excluir a conta: ${it.exception!!.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    finish()
                }
            } else {
                Toast.makeText(
                    this@ExcluirContaActivity,
                    "Erro ao reautenticar o usuário!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    lateinit var authCredential: AuthCredential
    private fun recuperarUsuario() {
        val construtor = AlertDialog.Builder(this)
            .setCancelable(false)
            .setView(R.layout.tela_carregando)

        val alertDialog = construtor.create()
        alertDialog.show()
        val usuarioRef = getDatabaseReference()!!.child("usuarios").child(getIdUsuarioLogado()!!)

        usuarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usuarioLogado = snapshot.getValue(Usuario::class.java)!!
                authCredential = EmailAuthProvider.getCredential(
                    usuarioLogado!!.email!!,
                    decode(usuarioLogado!!.senha!!)
                )
                alertDialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                alertDialog.dismiss()
                Toast.makeText(
                    this@ExcluirContaActivity,
                    "Erro: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun decrementarCidade(cidade: String?) {
        val cidadeIncrementoRef = getDatabaseReference()!!.child("cidade_usuarios").child(cidade!!)

        cidadeIncrementoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var numeroUsuariosCidade: Int = snapshot.getValue(Int::class.java)!!
                numeroUsuariosCidade--
                cidadeIncrementoRef.setValue(numeroUsuariosCidade)

                if (numeroUsuariosCidade == 0)
                    cidadeIncrementoRef.removeValue()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun salvarMotivoExclusao(idUsuarioLogado: String) {
        val motivoExclusaoRef =
            getDatabaseReference()!!.child("contas").child("exclusao").child(idUsuarioLogado)
                .child("motivo")

        val motivo = textoMotivoExclusaoConta.text.toString()
        motivoExclusaoRef.setValue(motivo)
    }

    private fun configurarToolbar() {
        val toolbar = binding.includeToolbar.toolbar
        toolbar.title = "Excluir conta"
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

    override fun onStart() {
        super.onStart()
        if (getFirebaseAuth()!!.currentUser == null) {
            finish()
        }
    }
}