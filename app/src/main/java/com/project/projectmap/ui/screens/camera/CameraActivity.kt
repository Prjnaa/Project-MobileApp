package com.project.projectmap.ui.screens.camera

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.project.projectmap.R
import com.project.projectmap.ui.theme.ProjectmapTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class CameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(
                this,
                cameraXPermission,
                0
            )
        }
        setContent {
            ProjectmapTheme {
                val bottomSheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true
                )
                val scope = rememberCoroutineScope()
                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(
                            CameraController.IMAGE_CAPTURE or
                                    CameraController.IMAGE_ANALYSIS
                        )
                    }
                }
                val capturedImage = remember { mutableStateOf<Bitmap?>(null) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    CameraPreview(
                        controller = controller,
                        modifier = Modifier
                            .fillMaxSize()
                    )

                    IconButton(
                        onClick = {
                            controller.cameraSelector = if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }
                        },
                        modifier = Modifier
                            .offset(10.dp, 30.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.camera_switch),
                            contentDescription = "Switch Camera"
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IconButton(
                            onClick = {
                                takePhoto(controller) { bitmap ->
                                    capturedImage.value = bitmap
                                    scope.launch {
                                        bottomSheetState.show()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.camera_capture),
                                contentDescription = "Capture"
                            )
                        }
                    }

                    if (bottomSheetState.isVisible) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                scope.launch {
                                    bottomSheetState.hide()
                                }
                            },
                            sheetState = bottomSheetState,
                            containerColor = Color(0xFFF3E5F5),
                        ) {
                            PhotoBottomSheetContent(
                                onDismiss = {
                                    scope.launch {
                                        bottomSheetState.hide()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap) -> Unit
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    onPhotoTaken(image.toBitmap())
                    image.close() // Close the image to prevent memory leaks
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Capture failed: ${exception.message}")
                }
            }
        )
    }

    private fun hasCameraPermission(): Boolean {
        return cameraXPermission.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        val cameraXPermission = arrayOf(
            android.Manifest.permission.CAMERA
        )
    }
}
