package br.com.mockap.mlkittestapp

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import android.support.v4.content.FileProvider


class SmileDetectionActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_IMAGE_PICK = 2

    lateinit var firebaseOptions: FirebaseVisionFaceDetectorOptions

    var captureImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpViews()
        configFirebaseOptions()
    }

    private fun setUpViews() {
        pickPhoto.setOnClickListener {
            choosePhoto()
        }

        takePhoto.setOnClickListener {
            capturePhoto()
        }
    }

    private fun choosePhoto() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_PICK)
        } else {
            Toast.makeText(this, "No Activity capable of opening the camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun capturePhoto() {
        var photoFile: File? = null
        val dir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        try {
            photoFile = FileHelper(dir).createImageFile()
        } catch (e: IOException) {
            Toast.makeText(this, "Awn No, something real bad happened", Toast.LENGTH_SHORT).show()
        }

        if (photoFile != null) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            captureImageUri = FileProvider.getUriForFile(this,
                    "br.com.mockap.mlkittestapp.fileprovider",
                    photoFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, captureImageUri)

            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            } else {
                Toast.makeText(this, "No Activity capable of opening the camera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configFirebaseOptions() {
        firebaseOptions = FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.15f)
                .setTrackingEnabled(true)
                .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            var image: Bitmap?
            if (captureImageUri != null) {
                image = MediaStore.Images.Media.getBitmap(contentResolver, captureImageUri)
                if (image == null) {
                    image = data?.extras?.get("data") as Bitmap?
                }
                if (image != null) {
                    pickedImage.setImageBitmap(image)
                    faceDetect(image)
                }
            }
        }

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val uri = data?.data

            val image = MediaStore.Images.Media.getBitmap(contentResolver, uri)

            if (image != null) {
                pickedImage.setImageBitmap(image)
                faceDetect(image)
            }
        }
    }

    private fun faceDetect(image: Bitmap) {
        val firebaseImage = FirebaseVisionImage.fromBitmap(image)
        val detector = FirebaseVision.getInstance().getVisionFaceDetector(firebaseOptions)

        val result = detector.detectInImage(firebaseImage)
                .addOnSuccessListener {
                    processFaces(it)
                }.addOnFailureListener {
                    it.printStackTrace()
                    Toast.makeText(this, "Something bad happended", Toast.LENGTH_SHORT).show()
                }
    }

    private fun processFaces(faces: List<FirebaseVisionFace>) {

        if (faces.isEmpty()) {
            Toast.makeText(this, "Is there a face here?", Toast.LENGTH_SHORT).show()
        } else {
            for (face in faces) {
                if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                    val smileProb = face.smilingProbability
                    if (smileProb > 0.5) {
                        Toast.makeText(this, "Smily face :)", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Not a Smily face :(", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Is there a face here?", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
