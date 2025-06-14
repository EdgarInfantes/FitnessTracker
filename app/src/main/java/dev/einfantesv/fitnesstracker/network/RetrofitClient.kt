package dev.einfantesv.fitnesstracker.network

import dev.einfantesv.fitnesstracker.Screens.util.Constants.BASE_URL
import dev.einfantesv.fitnesstracker.data.remote.api.AuthApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("$BASE_URL/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
}