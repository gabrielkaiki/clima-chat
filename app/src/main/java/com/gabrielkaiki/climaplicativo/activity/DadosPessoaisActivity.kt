package com.gabrielkaiki.climaplicativo.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.gabrielkaiki.climaplicativo.R
import com.gabrielkaiki.climaplicativo.databinding.ActivityDadosPessoaisBinding
import com.gabrielkaiki.climaplicativo.api.LocalidadesServices
import com.gabrielkaiki.climaplicativo.model.Distrito
import com.gabrielkaiki.climaplicativo.model.Estado
import com.gabrielkaiki.climaplicativo.model.Usuario
import com.gabrielkaiki.climaplicativo.permissoes.Permissoes
import com.gabrielkaiki.climaplicativo.utils.*
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class DadosPessoaisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDadosPessoaisBinding
    private lateinit var usuarioAtual: Usuario
    private lateinit var nome: TextInputEditText
    private lateinit var email: TextInputEditText
    private lateinit var spinnerEstado: Spinner
    private lateinit var spinnerCidade: Spinner
    private lateinit var imagemPerfil: CircleImageView
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var botaoSalvar: Button
    private lateinit var linearLayoutEstados: LinearLayout
    private lateinit var linearLayoutMunicipios: LinearLayout
    private var SELECAO_FOTO = 0
    private var fotoPerfil: String? = null
    private var permission = arrayOf(
        Manifest.permission.CAMERA
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDadosPessoaisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Toolbar
        configurarToolbar()

        //Launcher
        inicializarLauncher()

        //Inicializar componentes
        inicializarComponentes()

        //Recuperar usuário
        recuperarUsuario()
    }

    private fun inicializarLauncher() {
        activityResultLauncher =
            registerForActivityResult(StartActivityForResult()) { resultado ->
                if (resultado.resultCode == RESULT_OK) {
                    val imagem: Bitmap
                    if (SELECAO_FOTO == 0) { //Câmera
                        val dadosImagem: Bitmap = resultado.data!!.extras!!.get("data") as Bitmap
                        imagem = dadosImagem
                    } else { //Galeria
                        val urlImagem = resultado.data!!.data
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(contentResolver, urlImagem!!)
                            imagem = ImageDecoder.decodeBitmap(source)
                        } else {
                            imagem = MediaStore.Images.Media.getBitmap(contentResolver, urlImagem)
                        }
                    }
                    imagemPerfil.setImageBitmap(imagem)
                    uploadImagem(imagem)
                }
            }
    }

    private fun uploadImagem(imagem: Bitmap) {
        val alertDialog = alertDialogPadrao(this)
        alertDialog.show()

        val imagemStorageRef =
            getStorageReference()!!.child("imagens")
                .child(getIdUsuarioLogado()!!)
                .child("perfil.jpg")

        val baos = ByteArrayOutputStream()
        imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val imagemArray = baos.toByteArray()

        val uploadTask = imagemStorageRef.putBytes(imagemArray)
        uploadTask.addOnCompleteListener(
            this@DadosPessoaisActivity
        ) {
            if (it.isSuccessful) {
                imagemStorageRef.downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val imgPerfil = it.result.toString()
                        fotoPerfil = imgPerfil
                    } else {
                        Toast.makeText(
                            this@DadosPessoaisActivity,
                            "Erro ao obter a url da imagem!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this@DadosPessoaisActivity,
                    "Erro ao fazer upload da imagem!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            alertDialog.dismiss()
        }
    }

    private fun inicializarComponentes() {
        nome = binding.textNomeConfig
        email = binding.textEmailConfig
        spinnerCidade = binding.spinnerCidadeConfig
        spinnerEstado = binding.spinnerEstadoConfig
        linearLayoutEstados = binding.linearLayoutCarregandoEstadosConfig
        linearLayoutMunicipios = binding.linearLayoutCarregandoMunicipiosConfig
        botaoSalvar = binding.botaoSalvarConfig
        imagemPerfil = binding.imagemPerfilDadosPessoais

        imagemPerfil.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Selecione uma opção")

            alertDialog.setPositiveButton("Câmera") { _, _ ->
                if (!Permissoes.validarPermissoes(permission, this, 1)) {
                    SELECAO_FOTO = 0
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    activityResultLauncher.launch(intent)
                }
            }

            alertDialog.setNegativeButton("Galeria") { _, _ ->
                SELECAO_FOTO = 1
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intent)
            }

            alertDialog.setNeutralButton("Remover") { _, _ ->
                imagemPerfil.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.fundo_perfil
                    )
                )
                fotoPerfil = null
            }

            val dialog = alertDialog.create()
            dialog.show()
        }

        spinnerEstado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                popularSpinnerDistritos()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        botaoSalvar.setOnClickListener {
            salvar()
        }


    }

    private fun salvar() {
        val nome = nome.text.toString()
        val email = email.text.toString()
        val estado = spinnerEstado?.selectedItem.toString()
        val cidade = spinnerCidade?.selectedItem.toString()

        if (nome.isNotEmpty()) {
            if (email.isNotEmpty()) {
                if (estado != "null") {
                    if (cidade != "null") {
                        val usuario = Usuario()
                        usuario.id = getIdUsuarioLogado()
                        usuario.nome = nome
                        usuario.email = email
                        usuario.estado = estado
                        usuario.cidade = cidade
                        usuario.senha = usuarioAtual.senha
                        usuario.imagemPerfil = fotoPerfil

                        if (!usuarioAtual.cidade.equals(usuario.cidade)) {
                            //Atualizar tópico fireBase messaging
                            val cidadeAntigaFormatada =
                                removerAcentosEEspacos(usuarioAtual.cidade!!)
                            FirebaseMessaging.getInstance()
                                .unsubscribeFromTopic(cidadeAntigaFormatada)
                            val cidadeNovaFormatada = removerAcentosEEspacos(cidade)
                            FirebaseMessaging.getInstance().subscribeToTopic(cidadeNovaFormatada)

                            //Decrementar e incrementar usuários/cidade firebase
                            decrementarUsuarioCidadeAntiga(usuarioAtual.cidade!!)
                            incrementarUsuarioNovaCidade(usuario.cidade!!)
                        }

                        //Salvar referência para foto de perfil
                        val fireBaseImagemRef =
                            getDatabaseReference()!!.child("referencias_imagensStorage_usuario").child(
                                getIdUsuarioLogado()!!
                            ).child("perfil")

                        fireBaseImagemRef.setValue(fotoPerfil)

                        //Salvar dados do usuário no banco de dados
                        val usuarioRef =
                            getDatabaseReference()!!.child("usuarios").child(getIdUsuarioLogado()!!)
                        usuarioRef.setValue(usuario)

                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Por favor, espere carregar as cidades!",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Por favor, espere carregar os estados!",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            } else {
                Toast.makeText(this, "Por favor, digite o seu e-mail!", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Por favor, digite o seu nome!", Toast.LENGTH_LONG).show()
        }
    }

    private fun incrementarUsuarioNovaCidade(cidade: String) {
        val cidadeNovaRef = getDatabaseReference()!!.child("cidade_usuarios").child(cidade)

        cidadeNovaRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    var usuariosCidadeNova = snapshot.getValue(Int::class.java)!!
                    usuariosCidadeNova++
                    cidadeNovaRef.setValue(usuariosCidadeNova)
                } else {
                    cidadeNovaRef.setValue(1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun decrementarUsuarioCidadeAntiga(cidade: String) {
        val cidadeAntigaRef = getDatabaseReference()!!.child("cidade_usuarios").child(cidade)

        cidadeAntigaRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var numeroUsuariosCidadeAntiga: Int = snapshot.getValue(Int::class.java)!!
                numeroUsuariosCidadeAntiga--
                cidadeAntigaRef.setValue(numeroUsuariosCidadeAntiga)

                if (numeroUsuariosCidadeAntiga == 0)
                    cidadeAntigaRef.removeValue()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun recuperarUsuario() {
        val usuarioRef = getDatabaseReference()!!.child("usuarios").child(getIdUsuarioLogado()!!)
        usuarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usuarioAtual = snapshot.getValue(Usuario::class.java)!!
                popularSpinnerEstados()

                nome.setText(usuarioAtual.nome)
                email.setText(usuarioAtual.email)

                //Definir foto perfil, caso exista
                if (!usuarioAtual.imagemPerfil.isNullOrEmpty()) {
                    val urlPerfil = usuarioAtual.imagemPerfil
                    fotoPerfil = urlPerfil
                    Picasso.get().load(Uri.parse(urlPerfil)).into(imagemPerfil)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private var primeiraChamadaPopularDistritos = true
    private fun popularSpinnerDistritos() {
        linearLayoutMunicipios.visibility = View.VISIBLE
        val identificadorEstado =
            idUf[spinnerEstado.selectedItem.toString()].toString()

        var listaDistritos: List<Distrito>

        val retrofit = getRetrofitLocalidades()

        val requisicao = retrofit.create(LocalidadesServices::class.java)
        requisicao.getDistritos(identificadorEstado, "nome")
            .enqueue(object : Callback<List<Distrito>> {
                override fun onResponse(
                    call: Call<List<Distrito>>,
                    response: Response<List<Distrito>>
                ) {
                    if (response.body() != null) {
                        listaDistritos = response.body()!!

                        val listaNomesDistritos: MutableList<String> = arrayListOf()
                        for (distrito in listaDistritos) {
                            listaNomesDistritos.add(distrito.nome)
                        }

                        val arrayAdapterDistrito =
                            ArrayAdapter(
                                this@DadosPessoaisActivity,
                                android.R.layout.simple_spinner_item,
                                listaNomesDistritos
                            )
                        arrayAdapterDistrito.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerCidade.adapter = arrayAdapterDistrito
                        if (primeiraChamadaPopularDistritos) {
                            val posicaoCidadeUsuario =
                                arrayAdapterDistrito.getPosition(usuarioAtual.cidade)
                            spinnerCidade.setSelection(posicaoCidadeUsuario)
                            primeiraChamadaPopularDistritos = false
                        }
                        linearLayoutMunicipios.visibility = View.GONE
                    } else {
                        Toast.makeText(
                            this@DadosPessoaisActivity,
                            "Erro ao fazer requisição",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<Distrito>>, t: Throwable) {
                    Toast.makeText(
                        this@DadosPessoaisActivity,
                        "Falha ao fazer requisição",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })

    }

    private var primeiraChamadaPopularEstados = true
    private fun popularSpinnerEstados() {
        linearLayoutEstados.visibility = View.VISIBLE
        var listaEstados: List<Estado>

        val retrofit = getRetrofitLocalidades()

        val requisicao = retrofit.create(LocalidadesServices::class.java)
        requisicao.getEstados("nome").enqueue(object : Callback<List<Estado>> {
            override fun onResponse(
                call: Call<List<Estado>>,
                response: Response<List<Estado>>
            ) {
                if (response.body() != null) {
                    listaEstados = response.body()!!

                    val listaNomesEstados: MutableList<String> = arrayListOf()
                    for (estado in listaEstados) {
                        listaNomesEstados.add(estado.nome)
                    }

                    val arrayAdapterEstado =
                        ArrayAdapter(
                            this@DadosPessoaisActivity,
                            android.R.layout.simple_spinner_item,
                            listaNomesEstados
                        )
                    arrayAdapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerEstado.adapter = arrayAdapterEstado

                    if (primeiraChamadaPopularEstados) {
                        val posicaoEstadoUsuario =
                            arrayAdapterEstado.getPosition(usuarioAtual.estado)
                        spinnerEstado.setSelection(posicaoEstadoUsuario)
                        primeiraChamadaPopularEstados = false
                    }

                    linearLayoutEstados.visibility = View.GONE
                } else {
                    Toast.makeText(
                        this@DadosPessoaisActivity,
                        "Erro ao fazer requisição",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Estado>>, t: Throwable) {
                Toast.makeText(
                    this@DadosPessoaisActivity,
                    "Falha ao fazer requisição: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("info", "Falha ao fazer requisição: ${t.message}")
            }

        })
    }

    private fun configurarToolbar() {
        val toolbar = binding.toolbarIncludeConfig.toolbar
        toolbar.title = "Dados pessoais"
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        for (permissaoResultado in grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                alertaPermissao()
            }
        }
    }

    private fun alertaPermissao() {
        val builder = AlertDialog.Builder(this)
            .setTitle("Permissão negada")
            .setMessage("Para usar a câmera é necessário aceitar a permissão.")
            .setCancelable(false)

        builder.setPositiveButton("Ok") { _, _ ->
        }

        val dialog = builder.create()
        dialog.show()
    }
}