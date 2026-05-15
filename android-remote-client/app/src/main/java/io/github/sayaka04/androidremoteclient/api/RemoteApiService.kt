package io.github.sayaka04.androidremoteclient.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RemoteApiService {

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