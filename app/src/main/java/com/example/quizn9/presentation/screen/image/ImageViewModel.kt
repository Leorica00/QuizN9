package com.example.quizn9.presentation.screen.image

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizn9.presentation.event.ImageEvent
import com.example.quizn9.presentation.state.ImageState
import com.example.taskn21.data.remote.common.HandleErrorStates
import com.example.taskn21.data.remote.common.Resource
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

class ImageViewModel: ViewModel() {
    private val _imageStateFlow = MutableStateFlow(ImageState())
    val imageStateFlow = _imageStateFlow.asStateFlow()

    fun onEvent(event: ImageEvent) {
        when(event) {
            is ImageEvent.SetCompressedImageBitmapEvent -> setCompressedImageBitmap(bitmap = event.bitmap)
            is ImageEvent.UploadImageEvent -> uploadImageToFirebaseStorage()
        }
    }

    private fun setCompressedImageBitmap(bitmap: Bitmap) {
        _imageStateFlow.update { currentState -> currentState.copy(image = bitmap) }
    }

    private fun uploadImageToFirebaseStorage() {
        val outputStream = ByteArrayOutputStream()
        _imageStateFlow.value.image?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val data = outputStream.toByteArray().toString().toUri()
        viewModelScope.launch {
            uploadImage(data).collect {
                when(it) {
                    is Resource.Loading -> _imageStateFlow.update { currentState -> currentState.copy(isLoading = it.loading) }
                    is Resource.Success -> _imageStateFlow.update { currentState -> currentState.copy(errorMessage = null) }
                    is Resource.Error -> {
                        _imageStateFlow.update { currentState -> currentState.copy(errorMessage = it.error.toString()) }
                    }
                }

            }
        }
    }

    private suspend fun uploadImage(imageUri: Uri): Flow<Resource<String>> = flow {
        emit(Resource.Loading(true))

        val storageRef = Firebase.storage.reference.child("images/${UUID.randomUUID()}/${imageUri.lastPathSegment}")
        val uploadTask = storageRef.putFile(imageUri).await()
        val imageUrl = uploadTask.storage.downloadUrl.await().toString()
        emit(Resource.Success(imageUrl))

    }.catch { e ->
        emit(Resource.Error(HandleErrorStates.handleException(e), e))
    }.onCompletion {
        emit(Resource.Loading(false))
    }
}