package com.gabrielkaiki.climaplicativo.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gabrielkaiki.climaplicativo.api.LocalidadesServices
import com.gabrielkaiki.climaplicativo.databinding.ActivityCadastroBinding
import com.gabrielkaiki.climaplicativo.model.Distrito
import com.gabrielkaiki.climaplicativo.model.Estado
import com.gabrielkaiki.climaplicativo.model.Usuario
import com.gabrielkaiki.climaplicativo.utils.*
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CadastroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCadastroBinding
    private lateinit var textNome: TextInputEditText
    private lateinit var textEmail: TextInputEditText
    private lateinit var textSenha: TextInputEditText
    private lateinit var spinnerCidade: Spinner
    private lateinit var spinnerEstado: Spinner
    private lateinit var botaoCadastrar: Button
    private lateinit var usuario: Usuario
    private lateinit var linearLayoutEstados: LinearLayout
    private lateinit var linearLayoutMunicipios: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Toolbar
        val toolbar = binding.toolbarInclude.toolbar
        toolbar.title = "Cadastro"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Inicializar componentes
        inicializarComponentes()
    }

    private fun inicializarComponentes() {
        textNome = binding.textNome
        textEmail = binding.textEmail
        textSenha = binding.textSenha
        spinnerEstado = binding.spinnerEstado
        spinnerCidade = binding.spinnerCidade
        botaoCadastrar = binding.botaoCadastrar
        linearLayoutEstados = binding.linearLayoutCarregandoEstados
        linearLayoutMunicipios = binding.linearLayoutCarregandoMunicipios

        botaoCadastrar.setOnClickListener {
            validarCampos()
        }

        //Popular spinner com todos os estados brasileiros através de requisição feita à api
        popularSpinnerEstados()

        spinnerEstado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                //Popular spinner com todos os distritos do estado selecionado através de requisição feita à api
                popularSpinnerDistritos()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }

    private fun validarCampos() {
        val nome = textNome.text.toString()
        val email = textEmail.text.toString()
        val senha = textSenha.text.toString()

        if (!nome.isNullOrEmpty()) {
            if (!email.isNullOrEmpty()) {
                if (!senha.isNullOrEmpty()) {
                    val usuario = Usuario()
                    usuario.nome = nome
                    usuario.email = email
                    usuario.senha = senha
                    usuario.estado = spinnerEstado.selectedItem.toString()
                    usuario.cidade = spinnerCidade.selectedItem.toString()
                    cadastrar(usuario)
                } else {
                    Toast.makeText(this, "Por favor, digite o sua senha.", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(this, "Por favor, digite o seu e-mail.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Por favor, digite o seu nome.", Toast.LENGTH_SHORT).show()
        }
    }

    var auth = getFirebaseAuth()
    private fun cadastrar(usuario: Usuario) {
        val dialog = alertDialogPadrao(this)
        dialog.show()

        auth!!.createUserWithEmailAndPassword(usuario.email!!, usuario.senha!!)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    usuario.id = getIdUsuarioLogado()
                    usuario.senha = encode(usuario.senha!!)
                    incrementarCidade(usuario.cidade)
                    if (usuario.salvar()) {
                        //Formatação da cidade de cadastro
                        val cidadeFormatada = removerAcentosEEspacos(usuario.cidade!!)

                        //Inscrição em tópico de nontificação
                        FirebaseMessaging.getInstance().subscribeToTopic(cidadeFormatada)

                        recuperarUsuario(dialog)
                    } else {
                        Toast.makeText(
                            this@CadastroActivity,
                            "Erro ao cadastrar o usuário!",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }
                } else {
                    val excecao: String = try {
                        throw it.exception!!
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        "Digite uma senha mais forte!"
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        "Por favor, digite um e-mail válido"
                    } catch (e: FirebaseAuthUserCollisionException) {
                        "Este conta já foi cadastrada"
                    } catch (e: Exception) {
                        "Erro ao cadastrar o usuário ${e.message}"
                    }
                    Toast.makeText(this@CadastroActivity, excecao, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
    }

    private fun incrementarCidade(cidade: String?) {
        val cidadeIncrementoRef = getDatabaseReference()!!.child("cidade_usuarios").child(cidade!!)

        cidadeIncrementoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    var numeroUsuariosCidade: Int = snapshot.getValue(Int::class.java)!!
                    numeroUsuariosCidade++
                    cidadeIncrementoRef.setValue(numeroUsuariosCidade)
                } else {
                    cidadeIncrementoRef.setValue(1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun recuperarUsuario(dialog: AlertDialog) {
        val fireBase = getDatabaseReference()
        val usuarioRef = fireBase!!.child("usuarios").child(getIdUsuarioLogado()!!)

        usuarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usuario = snapshot.getValue(Usuario::class.java)!!
                redirecionarParaActivityChat(usuario, dialog)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@CadastroActivity,
                    "Operação de recuperação de usuário foi cancelada.", Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun redirecionarParaActivityChat(usuario: Usuario, dialog: AlertDialog) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("usuario", usuario)
        startActivity(intent)
        dialog.dismiss()
        finish()
    }

    private fun popularSpinnerDistritos() {
        linearLayoutMunicipios.visibility = View.VISIBLE
        val identificadorEstado = idUf[spinnerEstado.selectedItem.toString()].toString()

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
                                this@CadastroActivity,
                                android.R.layout.simple_spinner_item,
                                listaNomesDistritos
                            )
                        arrayAdapterDistrito.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerCidade.adapter = arrayAdapterDistrito
                        linearLayoutMunicipios.visibility = View.GONE
                    } else {
                        Toast.makeText(
                            this@CadastroActivity,
                            "Erro ao fazer requisição",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<Distrito>>, t: Throwable) {
                    Toast.makeText(
                        this@CadastroActivity,
                        "Falha ao fazer requisição",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })

    }

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

                    val arrayAdapterCidade =
                        ArrayAdapter(
                            this@CadastroActivity,
                            android.R.layout.simple_spinner_item,
                            listaNomesEstados
                        )
                    arrayAdapterCidade.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerEstado.adapter = arrayAdapterCidade
                    linearLayoutEstados.visibility = View.GONE
                } else {
                    Toast.makeText(
                        this@CadastroActivity,
                        "Erro ao fazer requisição",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Estado>>, t: Throwable) {
                Toast.makeText(
                    this@CadastroActivity,
                    "Falha ao fazer requisição: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("info", "Falha ao fazer requisição: ${t.message}")
            }

        })
    }
}