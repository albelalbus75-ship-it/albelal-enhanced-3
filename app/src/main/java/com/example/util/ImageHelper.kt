package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageHelper {

    fun compressAndSaveBitmap(context: Context, originalBitmap: Bitmap): String? {
        return try {
            val imagesDir = File(context.filesDir, "product_images").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "prod_img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val targetFile = File(imagesDir, fileName)

            // Scale down if dimensions are huge
            val maxDimension = 1024
            val scaledBitmap = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
                val ratio = minOf(
                    maxDimension.toFloat() / originalBitmap.width,
                    maxDimension.toFloat() / originalBitmap.height
                )
                val targetWidth = (originalBitmap.width * ratio).toInt()
                val targetHeight = (originalBitmap.height * ratio).toInt()
                Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            } else {
                originalBitmap
            }

            FileOutputStream(targetFile).use { outStream ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream)
                outStream.flush()
            }

            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun compressAndSaveUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate sample size
            var sampleSize = 1
            val maxDimension = 1024
            while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val decodeStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(decodeStream, null, decodeOptions)
            decodeStream?.close()

            if (bitmap != null) {
                compressAndSaveBitmap(context, bitmap)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
