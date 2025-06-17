package com.example.aula01

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class MainActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 1001
    private lateinit var captureImageButton: Button
    private lateinit var validateButton: Button
    private lateinit var capturedImageView: ImageView
    private lateinit var extractedTextView: TextView

    private var extractedText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        captureImageButton = findViewById(R.id.captureImageButton)
        validateButton = findViewById(R.id.validateButton)
        capturedImageView = findViewById(R.id.capturedImageView)
        extractedTextView = findViewById(R.id.extractedTextView)


        captureImageButton.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        }


        validateButton.setOnClickListener {
            if (isValidCPF(extractedText)) {
                saveToFirebase("user123", extractedText) // Exemplo de userId
                Toast.makeText(this, "CPF válido e armazenado no Firebase!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "CPF inválido.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val photo: Bitmap = data?.extras?.get("data") as Bitmap
            capturedImageView.setImageBitmap(photo)
            processImage(photo)
        }
    }


    private fun processImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    extractedText = visionText.text
                    extractedTextView.text = extractedText
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    Toast.makeText(this, "Falha ao processar imagem.", Toast.LENGTH_SHORT).show()
                }
    }

    // Função para validar CPF com regex simples
    private fun isValidCPF(cpf: String): Boolean {
        val regex = Regex("^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})$")
        return regex.containsMatchIn(cpf)
    }


    private fun saveToFirebase(userId: String, cpf: String) {
        val database: DatabaseReference = FirebaseDatabase.getInstance().reference
        val userRef = database.child("users").child(userId)

        userRef.child("cpf").setValue(cpf)
        userRef.child("timestamp").setValue(System.currentTimeMillis())
    }
}
