package com.example.furever.helpers

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

object ImageUploadHelper {

    fun uploadProfileImage(
        uri: Uri,
        userId: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val storage = FirebaseStorage.getInstance()
        val ref = storage.reference
            .child("profile_images/$userId.jpg")  // sobreescribe siempre la misma

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        onSuccess(downloadUri.toString())
                    }
                    .addOnFailureListener {
                        onError("No se pudo obtener la URL de la imagen")
                    }
            }
            .addOnFailureListener {
                onError(it.message ?: "Error al subir la imagen")
            }
    }
}