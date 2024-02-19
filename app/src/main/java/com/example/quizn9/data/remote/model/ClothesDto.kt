package com.example.quizn9.data.remote.model

data class ClothesDto(
    val id: Int,
    val cover: String,
    val price: String,
    val title: String,
    val favorite: Boolean,
    val category: String
)