package br.com.mockap.mlkittestapp

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class FileHelper(val storageDir: File) {

    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"

        val image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        )
        return image
    }
}