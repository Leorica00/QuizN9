package com.example.quizn9.data.remote.service

import com.example.quizn9.data.remote.model.ClothesDto
import retrofit2.Response
import retrofit2.http.GET

interface ClothesApiService {
    @GET("df8d4951-2757-45aa-8f60-bf1592a090ce")
    suspend fun getClothes(): Response<List<ClothesDto>>
}