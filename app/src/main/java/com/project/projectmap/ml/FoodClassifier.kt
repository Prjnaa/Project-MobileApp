package com.project.projectmap.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class FoodClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val labels = mutableListOf<String>()

    companion object {
        private const val TAG = "FoodClassifier"
        private const val MODEL_PATH = "food101_model.tflite"
        private const val LABEL_PATH = "class_names.txt"
        private const val IMG_SIZE = 224
    }

    init {
        try {
            loadModel()
            loadLabels()
        } catch (e: Exception) {
            Log.e(TAG, "Initialization error: ${e.message}")
        }
    }

    private fun loadModel() {
        try {
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                setUseNNAPI(false) // Nonaktifkan NNAPI untuk debugging
            }
            interpreter = Interpreter(loadModelFile(), options)
            Log.d(TAG, "Model loaded successfully.")

            val inputShape = interpreter?.getInputTensor(0)?.shape()?.joinToString(", ")
            val inputType = interpreter?.getInputTensor(0)?.dataType()
            Log.d(TAG, "Model Input Shape: $inputShape")
            Log.d(TAG, "Model Input DataType: $inputType")

            val outputShape = interpreter?.getOutputTensor(0)?.shape()?.joinToString(", ")
            val outputType = interpreter?.getOutputTensor(0)?.dataType()
            Log.d(TAG, "Model Output Shape: $outputShape")
            Log.d(TAG, "Model Output DataType: $outputType")

        } catch (e: Exception) {
            Log.e(TAG, "Error loading model: ${e.message}")
            throw e
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabels() {
        try {
            context.assets.open(LABEL_PATH).bufferedReader().use { reader ->
                reader.forEachLine { line ->
                    labels.add(line.trim())
                }
            }
            Log.d(TAG, "Labels loaded successfully: ${labels.size} labels.")
            labels.forEachIndexed { index, label ->
                Log.d(TAG, "Label[$index]: $label")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading labels: ${e.message}")
            throw e
        }
    }

    suspend fun classifyImage(bitmap: Bitmap): String = withContext(Dispatchers.Default) {
        try {
            // Pastikan bitmap ARGB_8888
            val argbBitmap = if (bitmap.config != Bitmap.Config.ARGB_8888) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
            } else {
                bitmap
            }

            // Buat TensorImage dengan tipe FLOAT32
            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(argbBitmap)

            // Tidak ada normalisasi, hanya resize, sesuai dengan saat training
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(IMG_SIZE, IMG_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .build()

            val processedImage = imageProcessor.process(tensorImage)
            val inputBuffer = processedImage.buffer

            // Debug log
            inputBuffer.rewind()
            val firstBytes = ByteArray(12)
            inputBuffer.get(firstBytes)
            Log.d(TAG, "First 12 bytes of inputBuffer: ${firstBytes.joinToString(", ")}")
            inputBuffer.rewind()

            val inputFloats = FloatArray(inputBuffer.capacity() / 4)
            inputBuffer.asFloatBuffer().get(inputFloats)
            val mean = inputFloats.average()
            val std = sqrt(inputFloats.map { (it - mean) * (it - mean) }.average())
            Log.d(TAG, "Input Mean: $mean, Input Std: $std")

            val output = Array(1) { FloatArray(labels.size) }

            interpreter?.run(inputBuffer, output)

            Log.d(TAG, "Model output (probabilities): ${output[0].joinToString(", ")}")
            val sumProb = output[0].sum()
            Log.d(TAG, "Sum of probabilities: $sumProb")

            val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
            return@withContext if (maxIndex != -1 && output[0][maxIndex] > 0) {
                Log.d(TAG, "Predicted class index: $maxIndex (${labels[maxIndex]}) with value: ${output[0][maxIndex]}")
                labels[maxIndex]
            } else {
                Log.e(TAG, "No valid prediction found.")
                "Unknown"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during classification: ${e.message}")
            "Error"
        }
    }

    fun close() {
        interpreter?.close()
    }
}
