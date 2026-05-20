package io.github.sayaka04.androidremoteclient.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RemoteApiService {

    // --- NEW ENDPOINTS ---
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("devices")
    suspend fun getDevices(): Response<List<Device>>

    @GET("devices/{deviceId}/commands")
    suspend fun getCommands(@Path("deviceId") deviceId: String): Response<List<DeviceCommand>>


    // --- YOUR ORIGINAL ENDPOINTS ---
    @POST("commands/{deviceId}/request")
    suspend fun sendCommandRequest(
        @Path("deviceId") deviceId: String,
        @Body payload: CommandRequest
    ): Response<Unit>


    @GET("commands/{deviceId}")
    suspend fun requestHostData(
        @Path("deviceId") deviceId: String
    ): Response<HostResponseWrapper>

}