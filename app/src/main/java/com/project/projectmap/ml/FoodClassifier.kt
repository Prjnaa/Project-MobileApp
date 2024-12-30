package com.project.projectmap.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            interpreter = Interpreter(loadModelFile())
            Log.d(TAG, "Model loaded successfully.")
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
                    labels.add(line)
                }
            }
            Log.d(TAG, "Labels loaded successfully: ${labels.size} labels.")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading labels: ${e.message}")
            throw e
        }
    }

    /**
     * Mengklasifikasikan gambar dan mengembalikan label prediksi
     */
    suspend fun classifyImage(bitmap: Bitmap): String = withContext(Dispatchers.Default) {
        try {
            // Pastikan bitmap dalam format ARGB_8888
            val argbBitmap = if (bitmap.config != Bitmap.Config.ARGB_8888) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                bitmap
            }

            // Resize bitmap ke ukuran yang diinginkan
            val resizedBitmap = Bitmap.createScaledBitmap(argbBitmap, IMG_SIZE, IMG_SIZE, true)

            // Konversi bitmap ke ByteBuffer
            val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

            // Debug: Log beberapa byte dari inputBuffer
            inputBuffer.rewind()
            val firstBytes = ByteArray(12)
            inputBuffer.get(firstBytes)
            Log.d(TAG, "First 12 bytes of inputBuffer: ${firstBytes.joinToString(", ")}")
            inputBuffer.rewind()

            // Siapkan buffer output
            val output = Array(1) { FloatArray(labels.size) }

            // Jalankan inferensi
            interpreter?.run(inputBuffer, output)

            // Debug: Log nilai output sebelum softmax
            Log.d(TAG, "Raw model output: ${output[0].joinToString(", ")}")

            // Terapkan softmax jika diperlukan
            val probabilities = softmax(output[0])

            // Debug: Log nilai output setelah softmax
            for (i in labels.indices) {
                Log.d(TAG, "Class ${labels[i]}: ${probabilities[i]}")
            }

            // Cari indeks dengan probabilitas tertinggi
            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1

            // Log hasil prediksi
            if (maxIndex != -1) {
                Log.d(TAG, "Predicted class index: $maxIndex (${labels[maxIndex]}) with value: ${probabilities[maxIndex]}")
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

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(1 * IMG_SIZE * IMG_SIZE * 3 * 4) // float32
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(IMG_SIZE * IMG_SIZE)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in intValues) {
            // Ekstrak nilai RGB dan normalisasi sesuai dengan EfficientNetB0
            val r = ((pixelValue shr 16 and 0xFF) / 255.0f - 0.485f) / 0.229f
            val g = ((pixelValue shr 8 and 0xFF) / 255.0f - 0.456f) / 0.224f
            val b = ((pixelValue and 0xFF) / 255.0f - 0.406f) / 0.225f

            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)

            // Log nilai pertama untuk verifikasi
            if (byteBuffer.position() <= 12) { // Log 3 float values (r, g, b)
                Log.d(TAG, "Pixel RGB: ($r, $g, $b)")
            }
        }

        return byteBuffer
    }



    /**
     * Menambahkan fungsi softmax
     */
    private fun softmax(input: FloatArray): FloatArray {
        val max = input.maxOrNull() ?: 0f
        val expValues = input.map { Math.exp((it - max).toDouble()).toFloat() }
        val sum = expValues.sum()
        return expValues.map { it / sum }.toFloatArray()
    }

    /**
     * Menutup interpreter saat tidak diperlukan
     */
    fun close() {
        interpreter?.close()
    }
}
