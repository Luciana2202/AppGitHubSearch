package com.example.appgithubsearch.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.tracing.perfetto.handshake.protocol.Response
import com.example.appgithubsearch.R
import com.example.appgithubsearch.data.GitHubService
import com.example.appgithubsearch.domain.Repository
import com.example.appgithubsearch.ui.Adapter.RepositoryAdapter
import javax.security.auth.callback.Callback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnPesquisar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService
    lateinit var carregando: ProgressBar
    lateinit var icWifiOff : ImageView
    lateinit var txtWifiOff : TextView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayShowHomeEnabled(true)

        setContentView(R.layout.activity_main)
        setupView()
        showUserName()
        setupRetrofit()
        setupListeners()

    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnPesquisar = findViewById(R.id.btn_pesquisar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
        carregando = findViewById(R.id.pb_carregando)
        icWifiOff = findViewById(R.id.iv_wifi_off)
        txtWifiOff = findViewById(R.id.tv_wifi_off)

    }




    fun setupRetrofit() {

        val retrofit = Retrofit.Builder().baseUrl("https://api.gitHub.com/").addConverterFactory(GsonConverterFactory.create()).build()

        githubApi = retrofit.create(GitHubService::class.java)

    }
    //metodo responsavel por configurar os listeners click da tela
    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupListeners() {
        btnPesquisar.setOnClickListener {
            val conexao = isInternetAvailable()

            if (!conexao) {
                icWifiOff.isVisible = true
                txtWifiOff.isVisible = true
            } else {

                icWifiOff.isVisible = false
                txtWifiOff.isVisible = false

                val nomePesquisar = nomeUsuario.text.toString()
                getAllReposByUserName(nomePesquisar)
                saveUserLocal()
                listaRepositories.isVisible = false
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        return actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    fun getAllReposByUserName(userName: String) { //Método responsável por buscar todos os repositórios do usuário fornecido

        if (userName.isNotEmpty()) {

            carregando.isVisible = true

            githubApi.getAllRepositoriesByUser(userName)
                .enqueue(object : Callback<List<Repository>> {

                    override fun onResponse(
                        call: Call<List<Repository>>,
                        response: Response<List<Repository>>) {
                        if (response.isSuccessful) {

                            carregando.isVisible = false
                            listaRepositories.isVisible = true

                            val repositories = response.body()

                            repositories?.let {
                                setupAdapter(repositories)
                            }

                        } else {

                            carregando.isVisible = false

                            val context = applicationContext
                            Toast.makeText(context, R.string.response_error, Toast.LENGTH_LONG)
                                .show()
                        }
                    }

                    override fun onFailure(call: Call<List<Repository>>, t: Throwable) {

                        carregando.isVisible = false

                        val context = applicationContext
                        Toast.makeText(context, R.string.response_error, Toast.LENGTH_LONG).show()
                    }

                })
        }
    }

    fun setupAdapter(list: List<Repository>) {

        val adapter = RepositoryAdapter(
            this, list)

        listaRepositories.adapter = adapter
    }

    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal() {
        val usuarioInformado = nomeUsuario.text.toString()

        val sharedPreference = getPreferences(MODE_PRIVATE) ?: return
        with(sharedPreference.edit()) {
            putString("saved_username", usuarioInformado)
            apply()
        }

    }

    private fun showUserName() {
        val sharedPreference = getPreferences(MODE_PRIVATE) ?: return
        val ultimoPesquisado = sharedPreference.getString("saved_username", null)

        if (!ultimoPesquisado.isNullOrEmpty()) {
            nomeUsuario.setText(ultimoPesquisado)
        }
    }

}
