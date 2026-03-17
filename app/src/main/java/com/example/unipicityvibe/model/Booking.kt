package com.example.unipicityvibe.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime

/**
 * Data model representing a user's booking.
 */
data class Booking(
    val id: String = "",
    val userId: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val quantity: Int = 0,
    val totalPrice: Double = 0.0,
    val bookingDate: String = "",
    val status: String = "confirmed"
)

/**
 * Saves a new booking to Firestore and updates the event's available ticket count.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun saveBookingToFirestore(
    booking: Booking,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    if (currentUser == null) {
        onFailure("User not logged in")
        return
    }

    // Populate userId and current timestamp
    val finalBooking = booking.copy(
        userId = currentUser.uid,
        bookingDate = LocalDateTime.now().toString()
    )

    db.collection("bookings")
        .add(finalBooking)
        .addOnSuccessListener {
            // Decrement ticket count upon successful booking
            updateEventTickets(booking.eventId, booking.quantity)
            onSuccess()
        }
        .addOnFailureListener { e ->
            onFailure(e.message ?: "Unknown error")
        }
}

/**
 * Retrieves all bookings associated with the current logged-in user.
 */
fun fetchUserBookings(onResult: (List<Booking>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    if (currentUser == null) {
        onResult(emptyList())
        return
    }

    db.collection("bookings")
        .whereEqualTo("userId", currentUser.uid)
        .get()
        .addOnSuccessListener { result ->
            val bookings = result.toObjects(Booking::class.java)
            onResult(bookings)
        }
        .addOnFailureListener {
            onResult(emptyList())
        }
}

/**
 * Updates the available ticket count for an event using a Firestore Transaction.
 * This ensures data integrity if multiple users book simultaneously.
 */
fun updateEventTickets(eventId: String, quantityBought: Int) {
    val db = FirebaseFirestore.getInstance()
    val eventRef = db.collection("events").document(eventId)

    db.runTransaction { transaction ->
        val snapshot = transaction.get(eventRef)
        val currentTickets = snapshot.getLong("availableTickets") ?: 0

        if (currentTickets >= quantityBought) {
            transaction.update(eventRef, "availableTickets", currentTickets - quantityBought)
        }
    }
}