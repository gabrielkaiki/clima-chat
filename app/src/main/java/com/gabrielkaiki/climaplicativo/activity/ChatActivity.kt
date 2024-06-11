package com.gabrielkaiki.climaplicativo.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color.GRAY
import android.graphics.Color.WHITE
import android.graphics.ImageDecoder
import android.graphics.drawable.GradientDrawable
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.gabrielkaiki.climaplicativo.adapter.AdapterMensagens
import com.gabrielkaiki.climaplicativo.databinding.ActivityChatBinding
import com.gabrielkaiki.climaplicativo.model.Mensagem
import com.gabrielkaiki.climaplicativo.model.Usuario
import com.gabrielkaiki.climaplicativo.permissoes.Permissoes
import com.gabrielkaiki.climaplicativo.utils.*
import com.gabrielkaiki.climaplicativo.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private var recyclerViewMensagens: RecyclerView? = null
    private var imagemPerfilUsuarioAtual: CircleImageView? = null
    private var nomeUsuarioAtual: TextView? = null
    private var cidadeAtualToolbar: TextView? = null
    private var campoMensagem: EditText? = null
    private lateinit var botaoEnviar: Button
    private var botaoEnviarImagem: Button? = null
    private var usuarioAtual: Usuario? = null
    private var fireBase: DatabaseReference? = null
    private var usuarioRef: DatabaseReference? = null
    private var mensagensRef: DatabaseReference? = null
    private var storageReference: StorageReference? = null
    private val listaMensagens: MutableList<Mensagem?> = ArrayList<Mensagem?>()
    private var valueEventListenerMensagens: ValueEventListener? = null
    private var adapterMensagens: AdapterMensagens? = null
    private var auth: FirebaseAuth? = null
    private lateinit var ultimoAcesso: TextView
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    private val TIPO_MENSAGEM_TEXTO = 0
    private val TIPO_MENSAGEM_IMAGEM = 1
    private var linearLayoutDadosUsuarioAtual: LinearLayout? = null
    private var localizacaoAtual: String? = null
    private lateinit var linearLayoutCaixaTextoBotao: LinearLayout
    private lateinit var mensagemLocalizacao: TextView
    private lateinit var textBotaoDuvidas: TextView
    private var SELECAO = 0
    private var permission = arrayOf(
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (getFirebaseAuth()!!.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        //Toolbar
        configuracaoToolbar()

        //Inicializar componentes
        inicializarComponentes()

        //Activity launcher
        inicializarActivityLauncher()

        //Referências do fireBase
        definicaoReferenciasFireBase()

        //Configuracao Recycler Mensagens
        configuracaoRecyclerMensagens()

        //Eventos de clique
        addEventosClique()

        //Recuperar mensagens
        recuperarMensagens()
    }

    lateinit var locationCallback: LocationCallback

    @SuppressLint("MissingPermission")
    private fun verificarLocalizacao() {

        val dialog = alertDialogLocalizacao(this)
        dialog.show()

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.create()
        locationRequest.interval = 2 * 3000
        locationRequest.priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY

        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest).build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(settingsRequest).addOnSuccessListener {
        }.addOnFailureListener {
            dialog.dismiss()
            if (it is ResolvableApiException) {
                val resolvableApiException: ResolvableApiException = it
                resolvableApiException.startResolutionForResult(this, 1)
            }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationAvailability(var1: LocationAvailability) {
            }

            override fun onLocationResult(var1: LocationResult) {
                dialog.dismiss()
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                val localizacao =
                    LatLng(var1.lastLocation!!.latitude, var1.lastLocation!!.longitude)
                checarCidade(localizacao)
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun checarCidade(it: LatLng) {
        val dialogCarregando = alertDialogPadrao(this)
        dialogCarregando.show()

        val geocoder = Geocoder(this, Locale.getDefault())

        try {
            val listaEnderecos =
                geocoder.getFromLocation(it.latitude, it.longitude, 1)

            if (!listaEnderecos.isNullOrEmpty()) {
                val endereco = listaEnderecos[0]
                val cidade = endereco.subAdminArea
                localizacaoAtual = cidade
                val cidadeCadastro = usuarioAtual?.cidade
                cidadeAtual = cidadeCadastro
                cidadeAtualToolbar!!.text = cidadeAtual

                if (localizacaoAtual.equals(cidadeCadastro)) {
                    dialogCarregando.dismiss()
                    exibirCaixaTexto()
                    mensagensRef =
                        fireBase!!.child("mensagens").child(cidadeAtual!!)
                    recuperarMensagens()
                    recuperarUltimoAcesso()
                } else {
                    dialogCarregando.dismiss()
                    ocultarCaixaTexto()
                    mensagensRef =
                        fireBase!!.child("mensagens").child(cidadeAtual!!)
                    recuperarMensagens()
                    recuperarUltimoAcesso()
                    mensagemLocalizacao.text =
                        "Não será possível responder a pedidos de condições climáticas para ${cidadeCadastro}," +
                                " porque você está em ${localizacaoAtual}."
                }
            } else {
                dialogCarregando.dismiss()
                Toast.makeText(
                    this,
                    "Erro ao verificar a sua localização.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: IOException) {
            dialogCarregando.dismiss()
            Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun inicializarActivityLauncher() {
        activityResultLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                var bitmap: Bitmap? = null
                when (SELECAO) {
                    0 -> {
                        val url = result.data!!.data
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(contentResolver, url!!)
                            bitmap = ImageDecoder.decodeBitmap(source)
                        } else {
                            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, url)
                        }
                    }
                    1 -> {
                        bitmap = result.data!!.extras!!.get("data") as Bitmap
                    }
                }
                val dialog = alertDialogCustomizada(this, "Enviando imagem...")
                dialog.show()

                val mensagem = Mensagem()
                mensagem.tipoMensagem = TIPO_MENSAGEM_IMAGEM
                mensagem.idRemetente = getIdUsuarioLogado()
                mensagem.cidade = cidadeAtual
                mensagem.nomeRemetente = usuarioAtual!!.nome
                if (!usuarioAtual!!.imagemPerfil.isNullOrEmpty())
                    mensagem.imagemPerfilUsuario = usuarioAtual!!.imagemPerfil
                val dataHoraAtual = Date()
                val hora = SimpleDateFormat("dd/MM/yyyy   hh:mm").format(dataHoraAtual)
                mensagem.horario = hora
                uploadImagem(mensagem, bitmap!!, dialog)
            }
        }
    }

    private fun uploadImagem(mensagem: Mensagem, bitmap: Bitmap, dialog: AlertDialog) {

        val idImagem = UUID.randomUUID().toString()
        val imagemRef = storageReference!!.child("imagens")
            .child(mensagem.idRemetente!!).child(cidadeAtual!!).child("${idImagem}.jpeg")

        mensagem.idMensagemStorage = "${idImagem}.jpeg"

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val dadosImagem = baos.toByteArray()

        val uploadTask = imagemRef.putBytes(dadosImagem)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imagemRef.downloadUrl.addOnCompleteListener { task ->
                    mensagem.urlImagem = task.result.toString()

                    val fireBaseImagemRef =
                        getDatabaseReference()!!.child("referencias_imagensStorage_usuario").child(
                            getIdUsuarioLogado()!!
                        ).child(idImagem)

                    fireBaseImagemRef.setValue(mensagem.urlImagem)
                    salvarMensagem(mensagem, dialog)
                }
            } else {
                Toast.makeText(
                    this@ChatActivity,
                    "Ocorreu um problema ao salvar a imagem! Erro: ${task.exception!!.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            dialog.dismiss()
        }
    }

    private fun addEventosClique() {
        botaoEnviar.setOnClickListener {
            val dialog = alertDialogCustomizada(this, "Enviando mensagem...")
            dialog.show()

            val textoMensagem = campoMensagem!!.text.toString()
            if (!textoMensagem.isEmpty()) {
                //Animação envio
                YoYo.with(Techniques.FlipInY)
                    .repeat(0)
                    .playOn(botaoEnviar)

                val mensagem = Mensagem()
                mensagem.mensagem = textoMensagem
                mensagem.tipoMensagem = TIPO_MENSAGEM_TEXTO
                mensagem.idRemetente = usuarioAtual!!.id!!
                mensagem.cidade = cidadeAtual
                mensagem.nomeRemetente = usuarioAtual!!.nome
                if (!usuarioAtual!!.imagemPerfil.isNullOrEmpty())
                    mensagem.imagemPerfilUsuario = usuarioAtual!!.imagemPerfil
                val dataHoraAtual = Date()
                val hora = SimpleDateFormat("dd/MM/yyyy   hh:mm").format(dataHoraAtual)
                mensagem.horario = hora
                salvarMensagem(mensagem, dialog)
            } else {
                Toast.makeText(this, "Digite uma mensagem antes de enviar!", Toast.LENGTH_SHORT)
                    .show()
                dialog.dismiss()
            }
        }
        botaoEnviarImagem!!.setOnClickListener {
            var intent: Intent
            val builderDialog = AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Escolha uma opção")

            builderDialog.setPositiveButton("Galeria") { _, _ ->
                SELECAO = 0
                intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher!!.launch(intent)
            }

            builderDialog.setNegativeButton("Câmera") { _, _ ->
                if (!Permissoes.validarPermissoes(permission, this@ChatActivity, 1)) {
                    SELECAO = 1
                    intent =
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    activityResultLauncher!!.launch(intent)
                }
            }

            builderDialog.setNeutralButton("Fechar") { _, _ ->

            }

            val dialog = builderDialog.create()
            dialog.show()
        }

        textBotaoDuvidas.setOnClickListener {
            startActivity(Intent(this@ChatActivity, DuvidasActivity::class.java))
        }
    }

    private fun salvarMensagem(mensagem: Mensagem, dialog: AlertDialog) {
        //Referência do fireBase
        var mensagemRef = mensagensRef

        //Id mensagem
        mensagem.idMensagem = mensagemRef!!.push().key

        //Salvar mensagem
        mensagemRef = mensagemRef.child(mensagem.idMensagem!!)

        val mensagemRefUsuario =
            getDatabaseReference()!!.child("referencias_mensagens_usuario")
                .child(getIdUsuarioLogado()!!).child(mensagem.idMensagem!!)

        val referenciaMensagem = "/mensagens/${cidadeAtual}/${mensagem.idMensagem}"

        mensagemRefUsuario.setValue(referenciaMensagem)
        mensagemRef.setValue(mensagem)
        dialog.dismiss()
    }

    private var mensagemSelecionadaCheck = false
    private var mensagemSelecionada: Mensagem? = null
    private var posicaoMensagemSelecionada: Int? = null
    private var viewAdapter: View? = null

    private fun configuracaoRecyclerMensagens() {
        adapterMensagens = AdapterMensagens(listaMensagens, this)
        recyclerViewMensagens!!.setHasFixedSize(true)
        recyclerViewMensagens!!.layoutManager = LinearLayoutManager(this)
        recyclerViewMensagens!!.adapter = adapterMensagens

        recyclerViewMensagens!!.addOnItemTouchListener(
            RecyclerItemClickListener(
                this@ChatActivity,
                recyclerViewMensagens,
                object :
                    RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        if (mensagemSelecionadaCheck && posicaoMensagemSelecionada === position) {
                            view!!.setBackgroundColor(WHITE)


                            viewAdapter = null
                            mensagemSelecionada = null
                            menu_principal.getItem(0).isVisible = false
                            mensagemSelecionadaCheck = false
                            var cardView = view as CardView

                            val shape = GradientDrawable()
                            shape.shape = GradientDrawable.RECTANGLE
                            shape.setColor(WHITE)
                            shape.cornerRadius = 20f

                            view.setBackgroundDrawable(shape)

                        } else {
                            val mensagemParaVisualizacao = listaMensagens[position]
                            if (mensagemParaVisualizacao!!.tipoMensagem == 1) {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setDataAndType(
                                    Uri.parse(mensagemParaVisualizacao!!.urlImagem), "image/*"
                                )
                                startActivity(intent)
                            }
                        }
                    }

                    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                    }

                    override fun onLongItemClick(view: View?, position: Int) {
                        mensagemSelecionada = listaMensagens[position]!!
                        if (!mensagemSelecionadaCheck && (mensagemSelecionada!!.idRemetente.equals(
                                getIdUsuarioLogado()
                            ))
                        ) {
                            val shape = GradientDrawable()
                            shape.shape = GradientDrawable.RECTANGLE
                            shape.setColor(GRAY)
                            shape.cornerRadius = 20f

                            view!!.setBackgroundDrawable(shape)

                            viewAdapter = view
                            mensagemSelecionada = listaMensagens[position]!!
                            menu_principal.getItem(0).isVisible = true
                            mensagemSelecionadaCheck = true
                            posicaoMensagemSelecionada = position
                        } else {
                            mensagemSelecionada = null
                        }
                    }
                })
        )
    }

    private fun recuperarMensagens() {
        if (mensagensRef != null) {
            valueEventListenerMensagens =
                mensagensRef!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        listaMensagens.clear()
                        for (mensagem in snapshot.children) {
                            listaMensagens.add(mensagem.getValue(Mensagem::class.java))
                        }

                        adapterMensagens!!.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }

    }

    private fun definicaoReferenciasFireBase() {
        fireBase = getDatabaseReference()!!
        val idUsuarioAtual = getIdUsuarioLogado()!!
        usuarioRef = fireBase!!.child("usuarios").child(idUsuarioAtual)

        if (cidadeAtual != null) {
            mensagensRef =
                fireBase!!.child("mensagens").child(cidadeAtual!!)
        }

        auth = getFirebaseAuth()
        storageReference = getStorageReference()
    }

    private fun recuperarUsuario() {
        val dialog = alertDialogPadrao(this)
        dialog.show()

        usuarioRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usuarioAtual = snapshot.getValue(Usuario::class.java)
                if (usuarioAtual == null)
                    finish()

                if (localizacaoAtual.equals(usuarioAtual?.cidade)) {
                    cidadeAtual = localizacaoAtual
                    linearLayoutCaixaTextoBotao.visibility = View.VISIBLE
                }

                //Nome do usuário
                nomeUsuarioAtual!!.text = usuarioAtual!!.nome

                //Foto de perfil do usuário
                if (!usuarioAtual!!.imagemPerfil.isNullOrEmpty()) {
                    Picasso.get().load(Uri.parse(usuarioAtual!!.imagemPerfil))
                        .into(imagemPerfilUsuarioAtual)
                } else {
                    imagemPerfilUsuarioAtual!!.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@ChatActivity,
                            R.drawable.fundo_perfil
                        )
                    )
                }


                //Último acesso
                if (cidadeAtual != null) {
                    dialog.dismiss()
                    recuperarUltimoAcesso()
                    cidadeAtualToolbar?.text = cidadeAtual
                }

                if (!notificacao) {
                    if (cidadeAtual.equals(usuarioAtual!!.cidade)) {
                        exibirCaixaTexto()
                    } else {
                        ocultarCaixaTexto()
                        mensagemLocalizacao.text =
                            "Somente moradores de ${cidadeAtual} poderão enviar mensagens/imagens sobre a condição climática."
                    }
                } else {
                    dialog.dismiss()
                    textBotaoDuvidas.visibility = View.VISIBLE
                    if (localizacaoAtual == null) verificarLocalizacao()
                }
                dialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
            }
        })
    }

    private fun exibirCaixaTexto() {
        linearLayoutCaixaTextoBotao.visibility = View.VISIBLE
        mensagemLocalizacao.visibility = View.GONE
    }

    private fun ocultarCaixaTexto() {
        linearLayoutCaixaTextoBotao.visibility = View.GONE
        mensagemLocalizacao.visibility = View.VISIBLE
    }

    private fun configuracaoToolbar() {
        val toolbar: Toolbar = binding.includeToolbar.toolbar
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun inicializarComponentes() {
        recyclerViewMensagens = binding.recyclerMensagens
        campoMensagem = binding.editMensagem
        botaoEnviar = binding.botaoEnviar
        botaoEnviarImagem = binding.botaoEnviarImagem
        imagemPerfilUsuarioAtual = binding.includeToolbar.imagemPerfilUsuarioAtual
        cidadeAtualToolbar = binding.textCidadeExibicao
        nomeUsuarioAtual = binding.includeToolbar.textNomeUsuarioAtual
        linearLayoutDadosUsuarioAtual = binding.includeToolbar.linearLayoutDadosUsuarioAtual
        linearLayoutDadosUsuarioAtual!!.visibility = View.VISIBLE
        ultimoAcesso = binding.includeToolbar.textUltimaHoraAcesso
        linearLayoutCaixaTextoBotao = binding.linearLayoutInputBotao
        mensagemLocalizacao = binding.textNotificacaoLocalizacao
        textBotaoDuvidas = binding.textDuvidasNotificacao
    }

    lateinit var menu_principal: Menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu_principal = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_home -> {
                startActivity(Intent(this, MapsActivity::class.java))
            }

            R.id.menu_deletar -> {
                val mensagemRef = getDatabaseReference()!!.child("mensagens").child(cidadeAtual!!)
                    .child(mensagemSelecionada!!.idMensagem!!)
                if (mensagemSelecionada!!.tipoMensagem == 1) {
                    val idMensagemStorage = mensagemSelecionada!!.idMensagemStorage
                    deletarImagemStorage(idMensagemStorage)
                }

                //Resetar padrão variáveis de checagem de seleção
                viewAdapter!!.setBackgroundColor(WHITE)
                mensagemSelecionadaCheck = false
                mensagemSelecionada = null
                viewAdapter = null

                mensagemRef.removeValue()
                adapterMensagens!!.notifyItemRemoved(posicaoMensagemSelecionada!!)

                posicaoMensagemSelecionada = null
                menu_principal.getItem(0).isVisible = false
            }

            R.id.menu_configuracoes -> {
                val intent = Intent(this, ConfiguracoesActivity::class.java)
                intent.putExtra("usuario", usuarioAtual)
                startActivity(intent)
            }

            R.id.menu_sair -> {
                getFirebaseAuth()!!.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        return true
    }

    private fun deletarImagemStorage(idMensagemStorage: String?) {
        val storageRef = getStorageReference()!!.child("imagens").child(cidadeAtual!!).child(
            getIdUsuarioLogado()!!
        ).child(idMensagemStorage!!)

        storageRef.delete()
    }

    override fun onStop() {
        super.onStop()
        mensagensRef!!.removeEventListener(valueEventListenerMensagens!!)
        val ultimoAcesso =
            fireBase!!.child("ultimoAcesso").child(usuarioAtual!!.id!!).child(cidadeAtual!!)
        val hora = Date()
        val horaFormatada = SimpleDateFormat("hh:mm").format(hora)
        ultimoAcesso.setValue(horaFormatada)
    }

    override fun onStart() {
        super.onStart()
        if (getFirebaseAuth()!!.currentUser != null) {
            recuperarUsuario()
            recuperarMensagens()
        } else {
            finish()
        }
    }

    private fun recuperarUltimoAcesso() {
        val ultimoHoraAcesso =
            fireBase!!.child("ultimoAcesso").child(getIdUsuarioLogado()!!).child(cidadeAtual!!)

        ultimoHoraAcesso.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null)
                    ultimoAcesso.text = snapshot.getValue(String::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
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

        builder.setPositiveButton("Fechar") { _, _ ->
        }

        val dialog = builder.create()
        dialog.show()
    }
}