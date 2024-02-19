package com.example.quizn9.presentation.state

import android.graphics.Bitmap

data class ImageState(
    val image: Bitmap? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)