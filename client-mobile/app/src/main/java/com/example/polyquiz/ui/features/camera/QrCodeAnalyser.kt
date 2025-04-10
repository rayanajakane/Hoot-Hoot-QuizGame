package com.example.polyquiz.ui.features.camera

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

// Ref : Our lord and savior Philipp Lackner
// https://www.youtube.com/watch?v=asl1mFtkMkc

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit,
): ImageAnalysis.Analyzer {

    // To be able to scan QR codes
    private val supportedImageFormats = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888
    )

    override fun analyze(image: ImageProxy) {
        Log.d("Qr analyser", "Got image with format ${image.format}")
        Log.d("Qr analyser", "Image dimensions: ${image.width}x${image.height}")

        if(image.format in supportedImageFormats) {
            // Get raw data
            val bytes = image.planes.first().buffer.toByteArray()
            Log.d("Qr analyser", "Got bytes: $bytes")

            val source = PlanarYUVLuminanceSource(
                bytes,
                image.width,
                image.height,
                0,
                0,
                image.width,
                image.height,
                false
            )

            // Bitmap file with info about QR code
            val binaryBmp = BinaryBitmap(HybridBinarizer(source))

            // Need to decode
            // Need to put in try-catch block because will throw exception everytime no QRcode in frame
            // TODO : Check device rotation. Make sure QR is recognised
            try{
                val result = MultiFormatReader().apply {
                    // Defined map of supported types of codes to scan
                    setHints(
                        mapOf(
                            DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
                                BarcodeFormat.QR_CODE
                            )
                        )
                    )
                }.decode(binaryBmp)

                onQrCodeScanned(result.text)
                Log.d("Reading QR", "Reading: ${result.text}")
            } catch (e: com.google.zxing.NotFoundException) {
                Log.d("QR Analysis", "No QR code found")
            } catch (e: Exception) {
                Log.e("QR Analysis", "Error processing image", e)
            } finally {
                image.close()
            }

        }
    }

    // Function to convert buffer to byte array
    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind() // To start at the beginning of our byteArray
        return ByteArray(remaining()).also {
            get(it) // Get all bytes in the byteArray
        } // Include the whole byte buffer
    }
}
