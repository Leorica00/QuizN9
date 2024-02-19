package com.example.quizn9.presentation.screen.image

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.quizn9.databinding.FragmentImageBinding
import com.example.quizn9.presentation.event.ImageEvent
import com.example.quizn9.presentation.extension.showSnackBar
import com.example.quizn9.presentation.screen.image.bottomsheet.BottomSheetFragment
import com.example.quizn9.presentation.screen.image.bottomsheet.BottomSheetListener
import com.example.quizn9.presentation.state.ImageState
import com.example.taskn21.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


@AndroidEntryPoint
class ImageFragment : BaseFragment<FragmentImageBinding>(FragmentImageBinding::inflate),
    BottomSheetListener {
    private lateinit var modalBottomSheet: BottomSheetFragment
    private val viewModel: ImageViewModel by viewModels()

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap?
            imageBitmap?.let {
                val compressedImage = compressBitmap(it)
                viewModel.onEvent(ImageEvent.SetCompressedImageBitmapEvent(compressedImage))}
        } else {
            Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    private val someActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                val compressedImage = compressImage(uri, 80)
                compressedImage?.let {
                    viewModel.onEvent(ImageEvent.SetCompressedImageBitmapEvent(it))
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun setUp() {

    }

    override fun setUpListeners() {
        binding.btnSelectImage.setOnClickListener {
            setUpBottomSheet()
        }

        binding.btnUploadImage.setOnClickListener {
            viewModel.onEvent(ImageEvent.UploadImageEvent)
        }
    }

    override fun setUpObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.imageStateFlow.collect {
                    handleState(it)
                }
            }
        }
    }

    private fun handleState(state: ImageState) {
        state.image?.let {
            binding.imageViewSelectedImage.setImageBitmap(it)
        }

        binding.progressBar.isVisible = state.isLoading

        state.errorMessage?.let {
            binding.root.showSnackBar(it)
        }
    }

    private fun setUpBottomSheet() {
        modalBottomSheet = BottomSheetFragment()
        modalBottomSheet.setBottomSheetListener(this)
        modalBottomSheet.show(parentFragmentManager, BottomSheetFragment.BOTTOM_SHEET)
    }

    override fun onImageFromStorageClicked() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        someActivityResultLauncher.launch(intent)
    }

    override fun onTakePhotoClicked() {
        if (checkCameraPermission()) {
            openCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }

    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun compressImage(uri: Uri, quality: Int): Bitmap? {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        inputStream?.use { input ->
            val bitmap = BitmapFactory.decodeStream(input)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            return BitmapFactory.decodeStream(outputStream.toByteArray().inputStream())
        }
        return null
    }
}
