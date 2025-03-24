package com.example.polyquiz.ui.features.camera

import android.graphics.ImageFormat
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
    private val onQrCodeScanned: (String) -> Unit
): ImageAnalysis.Analyzer {

    // To be able to scan QR codes
    private val supportedImageFormats = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888
    )

    override fun analyze(image: ImageProxy) {
        if(image.format in supportedImageFormats) {
            // Get raw data
            val bytes = image.planes.first().buffer.toByteArray()

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
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // Free resources
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
