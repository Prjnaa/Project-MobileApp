package com.project.projectmap.ui.screens.camera

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.project.projectmap.R
import com.project.projectmap.ml.FoodClassifier
import com.project.projectmap.ui.theme.ProjectmapTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
class CameraActivity : ComponentActivity() {
    private lateinit var foodClassifier: FoodClassifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Meminta izin kamera jika belum diberikan
        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(this, cameraXPermission, 0)
        }

        // Inisialisasi FoodClassifier
        // Pastikan FoodClassifier sudah diupdate agar tidak melakukan normalisasi
        // jika model dilatih tanpa normalisasi.
        foodClassifier = FoodClassifier(this)

        setContent {
            ProjectmapTheme {
                val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val scope = rememberCoroutineScope()
                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA)
                        setEnabledUseCases(CameraController.IMAGE_CAPTURE)
                    }
                }
                val capturedImage = remember { mutableStateOf<Bitmap?>(null) }
                var predictedFoodName by remember { mutableStateOf("") }
                var showBottomSheet by remember { mutableStateOf(false) }

                // Layout utama
                Box(modifier = Modifier.fillMaxSize()) {
                    // Preview kamera
                    CameraPreview(controller = controller, modifier = Modifier.fillMaxSize())

                    // Menampilkan gambar yang ditangkap
                    capturedImage.value?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Captured Image",
                            modifier = Modifier
                                .size(200.dp)
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        )
                    }

                    // Tombol capture foto
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(32.dp)),
                            onClick = {
                                takePhoto(controller) { bitmap ->
                                    if (bitmap != null) {
                                        capturedImage.value = bitmap

                                        // Proses klasifikasi makanan
                                        scope.launch {
                                            val prediction = foodClassifier.classifyImage(bitmap)
                                            Log.d("CameraActivity", "Prediction: $prediction")
                                            predictedFoodName = prediction
                                            showBottomSheet = true

                                            // Tampilkan hasil prediksi
                                            runOnUiThread {
                                                Toast.makeText(
                                                    this@CameraActivity,
                                                    "Predicted: $prediction",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    } else {
                                        runOnUiThread {
                                            Toast.makeText(
                                                this@CameraActivity,
                                                "Failed to capture image.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.camera_capture),
                                contentDescription = "Capture",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Bottom Sheet untuk menampilkan hasil prediksi
                    if (showBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                showBottomSheet = false
                            },
                            sheetState = bottomSheetState,
                            containerColor = MaterialTheme.colorScheme.surface,
                        ) {
                            PhotoBottomSheetContent(
                                foodName = predictedFoodName,
                                onDismiss = { showBottomSheet = false },
                                onSubmitSuccess = {
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Fungsi untuk mengambil foto
    private fun takePhoto(controller: LifecycleCameraController, onPhotoTaken: (Bitmap?) -> Unit) {
        controller.takePicture(ContextCompat.getMainExecutor(applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    val bitmap = image.toBitmap()
                    image.close() // Tutup image untuk mencegah memory leaks

                    if (bitmap != null) {
                        // Putar bitmap sesuai rotationDegrees
                        val rotationDegrees = image.imageInfo.rotationDegrees
                        val rotatedBitmap = rotateBitmap(bitmap, rotationDegrees)
                        Log.d("CameraActivity", "Captured image rotated by $rotationDegrees degrees.")

                        onPhotoTaken(rotatedBitmap)
                    } else {
                        Log.e("CameraActivity", "Bitmap conversion failed.")
                        onPhotoTaken(null)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("CameraActivity", "Capture failed: ${exception.message}")
                    onPhotoTaken(null)
                }
            })
    }

    // Fungsi untuk mengonversi ImageProxy ke Bitmap
    private fun ImageProxy.toBitmap(): Bitmap? {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Copy Y ke NV21
        yBuffer.get(nv21, 0, ySize)

        // Copy V dan U ke NV21 (Y + V + U)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        return try {
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
            val imageBytes = out.toByteArray()
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            Log.e("CameraActivity", "Error converting ImageProxy to Bitmap: ${e.message}")
            null
        }
    }

    // Fungsi untuk memutar bitmap sesuai derajat rotasi
    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // Memeriksa izin kamera
    private fun hasCameraPermission(): Boolean {
        return cameraXPermission.all {
            ContextCompat.checkSelfPermission(applicationContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        foodClassifier.close()
    }

    companion object {
        val cameraXPermission = arrayOf(android.Manifest.permission.CAMERA)
    }
}
