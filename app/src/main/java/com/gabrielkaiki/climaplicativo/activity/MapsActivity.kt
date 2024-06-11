package com.gabrielkaiki.climaplicativo.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.gabrielkaiki.climaplicativo.api.ClimaServices
import com.gabrielkaiki.climaplicativo.model.Clima
import com.gabrielkaiki.climaplicativo.permissoes.Permissoes
import com.gabrielkaiki.climaplicativo.utils.*
import com.gabrielkaiki.climaplicativo.R
import com.ferfalk.simplesearchview.SimpleSearchView
import com.gabrielkaiki.climaplicativo.databinding.ActivityMapsBinding
import com.github.clans.fab.FloatingActionButton
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.create
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var searchView: SimpleSearchView
    private lateinit var fabBotaoPesquisa: FloatingActionButton
    private lateinit var fabBotaoGps: FloatingActionButton
    private lateinit var fabBotaoAjuda: FloatingActionButton
    private lateinit var adView: AdView
    private lateinit var fabNotification: com.google.android.material.floatingactionbutton.FloatingActionButton
    private var permission = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.INTERNET
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Sdk Anúncios
        MobileAds.initialize(this)

        //Inicializar componentes e clickListeners
        inicializarComponentes()

        //SearchView
        searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextCleared(): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                pesquisarPrevisaoPorEndereco(query)
                searchView.closeSearch(true)
                return true
            }


        })

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun carregarAnuncios() {
        adView = binding.adViewMaps
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun inicializarComponentes() {
        fabBotaoPesquisa = binding.fabPesquisa
        fabBotaoGps = binding.fabGps
        fabBotaoAjuda = binding.fabAjuda
        fabNotification = binding.fabNotificacoes


        val bundle = intent.extras
        if (bundle != null) {
            notificacao = true
            fabNotification.visibility = View.VISIBLE

            YoYo.with(Techniques.Bounce)
                .duration(700)
                .repeat(15)
                .playOn(fabNotification)

            fabNotification.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        fabBotaoPesquisa.setOnClickListener {
            searchView.showSearch(true)
        }

        fabBotaoGps.setOnClickListener {
            //Permissões
            if (!Permissoes.validarPermissoes(permission, this, 1)) {
                obterLocalizacaoViaGps()
            }

        }

        fabBotaoAjuda.setOnClickListener {
            startActivity(Intent(this, AjudaActivity::class.java))
        }
    }

    private fun pesquisarPrevisaoPorEndereco(endereco: String) {
        val alert = alertDialogPadrao(this)
        alert.show()

        val retrofit = getRetrofit()
        val requisicao = retrofit.create(ClimaServices::class.java)

        requisicao.getClimaCidade(clima_api_key, "json-cors", endereco)
            .enqueue(object : Callback<Clima> {
                override fun onResponse(call: Call<Clima>, response: Response<Clima>) {
                    if (response.body() != null) {
                        notificacao = false
                        val clima: Clima = response.body()!!
                        cidadeAtual = clima.results!!.city_name
                        val intent = Intent(this@MapsActivity, InfoClimaLocalActivity::class.java)
                        intent.putExtra("clima", clima)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@MapsActivity,
                            "Erro ao consultar o clima do endereço informado!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    alert.dismiss()
                }

                override fun onFailure(call: Call<Clima>, t: Throwable) {
                    Toast.makeText(this@MapsActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    alert.dismiss()
                }
            })
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val brasilia = LatLng(-15.792428, -47.934623)
        mMap.addMarker(MarkerOptions().position(brasilia).title("Marcador"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(brasilia, 4f))

        //Fazer pesquisa via clique
        mMap.setOnMapClickListener {
            checarLocalTocadoNaTela(it)
        }
    }

    private fun checarLocalTocadoNaTela(it: LatLng) {
        val dialogCarregando = alertDialogPadrao(this)
        dialogCarregando.show()

        val geocoder = Geocoder(this, Locale.getDefault())

        try {
            val listaEnderecos =
                geocoder.getFromLocation(it.latitude, it.longitude, 1)

            if (!listaEnderecos.isNullOrEmpty()) {
                val endereco = listaEnderecos[0]
                val cidade = TextView(this)
                cidade.text = endereco.locality
                val local = LatLng(endereco.latitude, endereco.longitude)
                obterLocalizacaoViaLatLon(local)
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Nenhuma localização encontrada!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            runOnUiThread {
                dialogCarregando.dismiss()
            }
        } catch (e: IOException) {
            runOnUiThread {
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                dialogCarregando.dismiss()
                e.printStackTrace()
            }
        }
    }

    private fun obterLocalizacaoViaLatLon(localizacao: LatLng) {
        val dialog = alertDialogLocalizacao(this)
        dialog.show()

        val retrofit = getRetrofit()
        val requisicao: ClimaServices = retrofit.create(ClimaServices::class.java)

        requisicao.getClima(
            clima_api_key,
            "json-cors",
            localizacao.latitude.toString(),
            localizacao.longitude.toString(),
            "remote"
        ).enqueue(object : Callback<Clima> {
            override fun onResponse(call: Call<Clima>, response: Response<Clima>) {

                if (response.body() != null) {
                    notificacao = false
                    val clima: Clima = response.body()!!
                    cidadeAtual = clima.results!!.city_name
                    val intent = Intent(this@MapsActivity, InfoClimaLocalActivity::class.java)
                    intent.putExtra("clima", clima)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@MapsActivity,
                        "O local que você clicou é inválido!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dialog.dismiss()
            }

            override fun onFailure(call: Call<Clima>, t: Throwable) {
                Toast.makeText(this@MapsActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                dialog.dismiss()
            }
        })
    }

    lateinit var locationCallback: LocationCallback

    @SuppressLint("MissingPermission")
    private fun obterLocalizacaoViaGps() {

        val dialog = alertDialogLocalizacao(this)
        dialog.show()

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = create()
        locationRequest.interval = 2 * 3000
        locationRequest.priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY

        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest).build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(settingsRequest).addOnSuccessListener {
        }.addOnFailureListener {
            if (it is ResolvableApiException) {
                val resolvableApiException: ResolvableApiException = it
                resolvableApiException.startResolutionForResult(this, 1)
            }
        }

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(var1: LocationResult) {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                val localizacao =
                    LatLng(var1.lastLocation!!.latitude, var1.lastLocation!!.longitude)
                obterLocalizacaoViaLatLon(localizacao)
                dialog.dismiss()
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var permissoesNegadas = 0
        for (permissaoResultado in grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                alertaPermissao()
                permissoesNegadas++
            }
        }

        if (permissoesNegadas == 0) obterLocalizacaoViaGps()
    }

    private fun alertaPermissao() {
        val builder = AlertDialog.Builder(this)
            .setTitle("Permissões negadas")
            .setMessage("Para usar este recurso é necessário aceitar todas as permissões.")
            .setCancelable(false)

        builder.setPositiveButton("Ok") { _, _ ->
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        carregarAnuncios()
    }

    override fun onResume() {
        super.onResume()
        val codigoDeErro =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        when (codigoDeErro) {
            ConnectionResult.SERVICE_MISSING, ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED,
            ConnectionResult.SERVICE_DISABLED -> {
                dialogCodigoErro(codigoDeErro)
            }
        }
    }

    private fun dialogCodigoErro(codigoErro: Int) {
        GoogleApiAvailability.getInstance().getErrorDialog(
            this, codigoErro, 0
        ) { finish() }!!.show()
    }
}