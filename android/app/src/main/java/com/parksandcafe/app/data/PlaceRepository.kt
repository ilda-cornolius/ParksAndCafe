package com.parksandcafe.app.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.parksandcafe.app.PlaceType
import kotlinx.coroutines.tasks.await

data class PlaceDraft(
    val lat: Double,
    val lng: Double,
    val type: PlaceType,
    val name: String? = null,
    val notes: String? = null,
)

class PlaceRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun savePlace(draft: PlaceDraft): String {
        val user = auth.currentUser
            ?: throw IllegalStateException("Not signed in")

        val data = hashMapOf(
            "lat" to draft.lat,
            "lng" to draft.lng,
            "type" to draft.type.firestoreValue,
            "createdAt" to Timestamp.now(),
            "userId" to user.uid,
            "name" to draft.name,
            "notes" to draft.notes,
        )

        val ref = db.collection(COLLECTION).add(data).await()
        return ref.id
    }

    companion object {
        const val COLLECTION = "places"
    }
}
