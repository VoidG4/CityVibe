package com.example.unipicityvibe.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Data model representing an Event.
 * Default values are provided to support Firestore deserialization.
 */
data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val availableTickets: Int = 0,
    val date: String = "",
    val venue: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val imageUrl: String = ""
)

/**
 * Fetches all events from the Firestore "events" collection.
 * Maps the Firestore document ID to the Event object.
 */
fun fetchEventsFromFirestore(onResult: (List<Event>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("events")
        .get()
        .addOnSuccessListener { result ->
            val eventList = result.documents.map { doc ->
                val event = doc.toObject(Event::class.java)!!
                event.copy(id = doc.id)
            }
            onResult(eventList)
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error fetching events", e)
            onResult(emptyList())
        }
}

/**
 * Fetches a single event by its Document ID.
 */
fun fetchEventById(eventId: String, onResult: (Event?) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("events").document(eventId)
        .get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val event = document.toObject(Event::class.java)
                onResult(event?.copy(id = document.id))
            } else {
                onResult(null)
            }
        }
        .addOnFailureListener {
            onResult(null)
        }
}