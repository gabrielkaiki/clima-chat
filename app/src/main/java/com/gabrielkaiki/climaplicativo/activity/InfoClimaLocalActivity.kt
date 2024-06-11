package com.gabrielkaiki.climaplicativo.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.gabrielkaiki.climaplicativo.R
import com.gabrielkaiki.climaplicativo.databinding.ActivityInfoClimaLocalBinding
import com.gabrielkaiki.climaplicativo.adapter.AdapterPrevisao
import com.gabrielkaiki.climaplicativo.api.NotificacaoService
import com.gabrielkaiki.climaplicativo.model.*
import com.gabrielkaiki.climaplicativo.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class InfoClimaLocalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoClimaLocalBinding
    private lateinit var campoCidade: TextView
    private lateinit var campoClima: TextView
    private lateinit var campoTemperatura: TextView
    private lateinit var campoVento: TextView
    private lateinit var campoData: TextView
    private lateinit var campoHora: TextView

    companion object {
        public lateinit var textoSolicitacaoEnviada: TextView
    }

    private lateinit var textoSolicitacao: TextView
    private lateinit var campoHumidade: TextView
    private lateinit var numeroUsuariosCadastradosCidade: TextView
    private lateinit var campoImgClima: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var previsoes: ArrayList<DiaClima>
    private lateinit var adapterPrevisao: AdapterPrevisao
    private lateinit var botaoAcessarMensagens: Button
    private lateinit var botaoPesquisarNovamente: Button
    private lateinit var botaoPedirMensagens: TextView
    private lateinit var textoInformativoMensagens: TextView
    private lateinit var valueEventListenerMensagens: ValueEventListener
    private var mensagensRef: DatabaseReference? = null
    private lateinit var clima: Clima
    private lateinit var cidade: String
    private lateinit var progressBarCarregandoMensagens: ProgressBar
    private lateinit var progressBarSolicitacao: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInfoClimaLocalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Inicializar componentes e clickListeners
        inicializarComponentes()

        //Recuperar informações
        recuperarLocalClima()

        //Shared preferences
        var sharedPreferences = getSharedPreferences("preferencias", 0)
        var editor = sharedPreferences.edit()
        var numeroExecucoes = sharedPreferences.getInt("execucoes", 0)

        editor.putInt("execucoes", numeroExecucoes + 1)
        editor.apply()

        //Animação swipe
        var deslizeAnimator = binding.indicadorSwipe
        if (numeroExecucoes <= 1) {
            YoYo.with(Techniques.BounceInLeft)
                .duration(300)
                .repeat(10)
                .onEnd {
                    deslizeAnimator.visibility = View.GONE
                }
                .playOn(deslizeAnimator)
        } else {
            deslizeAnimator.visibility = View.GONE
        }
    }


    private fun verificarMensagens(cidade: String?) {
        mensagensRef = getDatabaseReference()!!.child("mensagens").child(cidade!!)

        valueEventListenerMensagens =
            mensagensRef!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var qtdMensagens: Int = snapshot.childrenCount.toInt()

                    when (qtdMensagens) {
                        0 -> {
                            textoInformativoMensagens.text =
                                "Nenhuma mensagem no chat para ${cidade}."
                        }
                        1 -> {
                            textoInformativoMensagens.text =
                                "${qtdMensagens} mensagem no chat para ${cidade}."
                        }
                        else -> {
                            textoInformativoMensagens.text =
                                "${qtdMensagens} mensagens no chat para ${cidade}."
                        }
                    }
                    progressBarCarregandoMensagens.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@InfoClimaLocalActivity,
                        "Erro: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            })

    }

    private fun inicializarComponentes() {
        campoCidade = binding.textCidade
        campoClima = binding.textClima
        campoImgClima = binding.imageClima
        campoTemperatura = binding.textTemp
        campoVento = binding.textVento
        campoData = binding.textData
        campoHora = binding.textHora
        campoHumidade = binding.textHumidade
        recyclerView = binding.recyclerPrevisao10Dias
        botaoAcessarMensagens = binding.botaoAcessarMensagens
        botaoPesquisarNovamente = binding.botaoFazerOutraPesquisa
        botaoPedirMensagens = binding.textPedirMensagens
        textoInformativoMensagens = binding.textInfoMensagensChat
        progressBarCarregandoMensagens = binding.progressBarCarregandoMensagens
        progressBarSolicitacao = binding.progressBarCarregandoPedido
        textoSolicitacao = binding.textSolicitacao
        textoSolicitacaoEnviada = binding.textoSolicitacaoEnviada
        numeroUsuariosCadastradosCidade = binding.textNumeroUsuariosCidade

        botaoPedirMensagens.setOnClickListener {
            progressBarSolicitacao.visibility = View.VISIBLE
            //Formatar string cidade atual
            val cidadeFormatada = removerAcentosEEspacos(cidadeAtual!!)

            val retrofit = getRetrofitNotificacao()

            //Parâmetros de notificação para firebase
            val to = "/topics/${cidadeFormatada}"

            val notificacao = Notification(
                "Para moradores de $cidadeAtual",
                "Um usuário pediu foto/mensagem sobre o clima."
            )

            //Corpo da notificação
            val notificacaoBase = NotificacaoBase(to, notificacao)

            val requisicao = retrofit.create(NotificacaoService::class.java)

            requisicao.enviar(notificacaoBase)?.enqueue(object : Callback<NotificacaoBase?> {
                override fun onResponse(
                    call: Call<NotificacaoBase?>,
                    response: Response<NotificacaoBase?>
                ) {
                    if (response.isSuccessful) {
                        progressBarSolicitacao.visibility = View.GONE
                        textoSolicitacaoEnviada.visibility = View.VISIBLE
                        botaoPedirMensagens.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.botao_verde,
                            0,
                            0,
                            0
                        )
                    } else {
                        Toast.makeText(
                            this@InfoClimaLocalActivity,
                            "Erro ao enviar solicitação!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<NotificacaoBase?>, t: Throwable) {
                    Log.d("info", "info: ${t.message}")
                }
            })
        }

        botaoAcessarMensagens.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        botaoPesquisarNovamente.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }
    }

    private fun definirTextoSolicitacao(cidade: String?) {
        textoSolicitacao.text =
            "Solicite mensagens para moradores de $cidade tocando no botão pedir mensagens abaixo."
        textoSolicitacao.visibility = View.VISIBLE

        botaoPedirMensagens.setCompoundDrawablesRelativeWithIntrinsicBounds(
            R.drawable.botao_vermelho,
            0,
            0,
            0
        )
        botaoPedirMensagens.visibility = View.VISIBLE
    }

    private fun configurarRecycler() {
        adapterPrevisao = AdapterPrevisao(previsoes, this)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapterPrevisao
    }

    private fun recuperarLocalClima() {
        val bundle = intent.extras
        if (bundle != null) {
            clima = bundle.getSerializable("clima") as Clima

            //Lista de previsões para os próximos 15 dias e configuração da recyclerView
            previsoes = clima.results!!.forecast
            configurarRecycler()

            //Método para atribuir imagem de acordo com a condição do clima
            campoClima.text = clima.results!!.description
            campoImgClima.setImageResource(codigoCondicao.get(clima.results!!.condition_code!!.toInt()) as Int)

            campoCidade.text = "${clima.results?.city_name}"
            campoTemperatura.text = "${clima.results?.temp} ºC"
            campoVento.text = clima.results?.wind_speedy
            campoData.text = clima.results?.date
            campoHora.text = clima.results?.time
            campoHumidade.text = clima.results?.humidity + "%"

            //Verificar mensagens para a região
            cidade = clima.results?.city_name.toString()
            cidadeAtual = cidade
        }
    }

    override fun onStop() {
        super.onStop()
        if (mensagensRef != null) {
            mensagensRef!!.removeEventListener(valueEventListenerMensagens)
        }
    }

    override fun onStart() {
        super.onStart()
        resetarVisibilidadeTextViews()
        verificarMensagens(cidadeAtual)
        verificarUsuarios(cidadeAtual)
    }

    private fun resetarVisibilidadeTextViews() {
        textoSolicitacaoEnviada.visibility = View.GONE
        textoSolicitacao.visibility = View.GONE
        botaoPedirMensagens.visibility = View.GONE
    }

    private fun verificarUsuarios(cidadeAtual: String?) {
        val usuariosCidadeRef =
            getDatabaseReference()!!.child("cidade_usuarios").child(cidadeAtual!!)

        usuariosCidadeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var usuariosCidade = 0
                if (snapshot.value != null)
                    usuariosCidade = snapshot.getValue(Int::class.java)!!

                when (usuariosCidade) {
                    0 -> {
                        numeroUsuariosCadastradosCidade.text =
                            "Nenhum usuário de ${cidadeAtual} está cadastrado no sistema."
                    }

                    1 -> {
                        numeroUsuariosCadastradosCidade.text =
                            "${usuariosCidade} usuário de ${cidadeAtual} cadastrado."
                        definirTextoSolicitacao(cidadeAtual)
                    }
                    else -> {
                        numeroUsuariosCadastradosCidade.text =
                            "${usuariosCidade} usuários de ${cidadeAtual} cadastrados."
                        definirTextoSolicitacao(cidadeAtual)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

}