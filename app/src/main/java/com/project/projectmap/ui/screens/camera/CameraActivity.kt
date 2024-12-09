package com.project.projectmap.ui.screens.camera

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
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
import java.nio.ByteBuffer

@OptIn(ExperimentalMaterial3Api::class)
class CameraActivity : ComponentActivity() {
    private lateinit var foodClassifier: FoodClassifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(this, cameraXPermission, 0)
        }

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
                val processedImage = remember { mutableStateOf<Bitmap?>(null) } // For showing the processed image
                var predictedFoodName by remember { mutableStateOf("") } // For predicted food name
                var showBottomSheet by remember { mutableStateOf(false) } // Bottom sheet visibility

                // Display camera UI
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(controller = controller, modifier = Modifier.fillMaxSize())

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .offset(y = (-32).dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IconButton(
                            modifier = Modifier.size(52.dp),
                            onClick = {
                                takePhoto(controller) { bitmap ->
                                    if (bitmap != null) {
                                        capturedImage.value = bitmap
                                        processedImage.value = bitmap // Show the processed image

                                        // Object detection to find the food region
                                        scope.launch {
                                            val foodRegion = detectFoodRegion(bitmap)
                                            if (foodRegion != null) {
                                                val croppedBitmap = cropImage(bitmap, foodRegion)
                                                // Predict food from cropped image
                                                val prediction = foodClassifier.classifyImage(croppedBitmap)
                                                Log.d("CameraActivity", "Prediction: $prediction")
                                                predictedFoodName = prediction
                                                showBottomSheet = true
                                            } else {
                                                Log.e("CameraActivity", "Food detection failed.")
                                            }
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.camera_capture),
                                contentDescription = "Capture",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }

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

    // Detect food region (example with placeholder logic)
    private suspend fun detectFoodRegion(bitmap: Bitmap): Rect? {
        // This method should implement object detection logic to find food in the image.
        // Placeholder logic - return a dummy rect (this should be replaced by actual object detection)
        return Rect(100, 100, 800, 600) // Example bounding box
    }

    // Crop the image based on detected food region
    private fun cropImage(bitmap: Bitmap, rect: Rect): Bitmap {
        return Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
    }

    // Take photo logic
    private fun takePhoto(controller: LifecycleCameraController, onPhotoTaken: (Bitmap?) -> Unit) {
        controller.takePicture(ContextCompat.getMainExecutor(applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    val bitmap = image.toBitmap()
                    image.close() // Close the image to prevent memory leaks

                    if (bitmap != null) {
                        // Determine rotation from metadata
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

    // Convert ImageProxy to Bitmap
    private fun ImageProxy.toBitmap(): Bitmap? {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Copy Y to NV21
        yBuffer.get(nv21, 0, ySize)

        // Copy V and U to NV21 (Y + V + U)
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

    // Rotate bitmap based on rotation degrees
    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // Check camera permission
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
