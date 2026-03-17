package com.example.unipicityvibe

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.unipicityvibe.model.Event
import com.google.android.gms.location.*

class LocationNotificationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    companion object {
        // Set to track events that have already triggered a notification
        private val notifiedEvents = mutableSetOf<String>()
    }

    fun startLocationUpdates(events: List<Event>) {
        // Check for location permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current high-accuracy location
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    checkProximityToEvents(location, events)
                } else {
                    Toast.makeText(context, "Location not found. Please enable GPS/Maps.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun checkProximityToEvents(userLocation: Location, events: List<Event>) {
        var eventsFoundNearby = 0

        for (event in events) {
            if (event.lat == 0.0 || event.lng == 0.0) continue

            val eventLocation = Location("event").apply {
                latitude = event.lat
                longitude = event.lng
            }

            val distanceInMeters = userLocation.distanceTo(eventLocation)

            // Radius set to 5000 meters for testing (adjust to 200m for production)
            if (distanceInMeters <= 5000) {
                // Only notify if not already notified for this event ID
                if (!notifiedEvents.contains(event.id)) {
                    sendNotification(event)
                    notifiedEvents.add(event.id)
                    eventsFoundNearby++
                }
            }
        }

        // Notify user if no new events are found (good UX feedback)
        if (eventsFoundNearby == 0 && notifiedEvents.isEmpty()) {
            Toast.makeText(context, "No new events found nearby.", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun sendNotification(event: Event) {
        createChannelIfNeeded()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_event_id", event.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, "event_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("Event Nearby!")
            .setContentText("You are close to ${event.title} (${event.venue})")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        showNotification(event.id.hashCode(), builder)
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("event_channel", "Nearby Events", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications for nearby events"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(id: Int, builder: NotificationCompat.Builder) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(id, builder.build())
        } else {
            Toast.makeText(context, "Notification Permission Missing!", Toast.LENGTH_SHORT).show()
        }
    }
}