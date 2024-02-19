package com.example.quizn9.presentation.event

import android.graphics.Bitmap

sealed interface ImageEvent {
    class SetCompressedImageBitmapEvent(val bitmap: Bitmap): ImageEvent
    object UploadImageEvent : ImageEvent
}