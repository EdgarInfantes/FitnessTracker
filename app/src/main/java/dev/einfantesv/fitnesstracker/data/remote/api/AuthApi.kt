package dev.einfantesv.fitnesstracker.data.remote.api

import dev.einfantesv.fitnesstracker.data.remote.dto.LoginRequest
import dev.einfantesv.fitnesstracker.data.remote.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call

interface AuthApi {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}
