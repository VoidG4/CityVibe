# 🏛️ UnipiCityVibe

UnipiCityVibe is a modern, feature-rich Android application designed to help users discover, track, and book tickets for cultural events in their city. Built entirely with **Kotlin** and **Jetpack Compose**, it offers a seamless and accessible user experience, featuring real-time geolocation alerts, voice-guided navigation, and a dynamic multilingual interface.

## ✨ Key Features

### 🔐 Authentication & Profile
* **User Authentication:** Secure Login and Registration powered by Firebase Authentication.
* **Personalized Dashboard:** Users can only access the app and their personal booking history after a successful login.

### 🎫 Event Discovery & Bookings
* **Live Event Feed:** Browse a dynamic list of events fetched in real-time from Cloud Firestore.
* **Smart Search & Filtering:** Filter events by category (Theater, Concert, Cinema, Festival, Sports, etc.) or search by title.
* **Seamless Booking System:** Select ticket quantities, view dynamic total pricing, and instantly secure bookings.
* **My Bookings:** A dedicated screen for users to view their active tickets, total events attended, and total amount spent.

### 📍 Smart Geolocation & Notifications
* **Proximity Alerts:** Uses `FusedLocationProviderClient` to track the user's location. If the user is within a 200-meter radius of an upcoming event, they receive an instant Push Notification.
* **Deep Linking:** Tapping the notification automatically navigates the user directly to the specific Event's details page.

### 🎙️ Accessibility & Voice Features (Bonus)
* **Voice Commands (Speech-to-Text):** A dedicated, visually responsive Voice Assistant screen. Users can say "Home", "Map", "Bookings", or "Settings" to navigate the app hands-free.
* **Text-to-Speech (TTS):** The app features voice feedback for navigation and a dedicated "Read Aloud" button on event pages to read descriptions to the user.
* **Multilingual UI:** Switch between English, Greek, and Spanish on the fly.

### 🎨 Modern UI/UX
* **Shadcn-Inspired Design:** Clean aesthetics, smooth gradients, and rounded components.
* **Dynamic Dark Mode:** Fully responsive Dark/Light themes that apply to the entire UI, including system status and navigation bars.
* **Preferences Memory:** User settings (Theme, Language, Notification Toggles) are saved locally using `SharedPreferences`.

---

## 🛠️ Tech Stack & Architecture

* **Language:** [Kotlin](https://kotlinlang.org/)
* **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
* **Navigation:** Jetpack Navigation Compose (Single-Activity Architecture)
* **Backend as a Service (BaaS):** [Firebase](https://firebase.google.com/)
  * Firebase Authentication
  * Cloud Firestore (NoSQL Database)
* **Location Services:** Google Play Services Location API
* **Image Loading:** [Coil](https://coil-kt.github.io/coil/) (Asynchronous image loading)
* **Local Storage:** Android `SharedPreferences`
* **Hardware APIs:** `SpeechRecognizer` (STT), `TextToSpeech` (TTS)

---

## 📸 Screenshots

| Home Screen | Event Details | Voice Navigation | My Bookings |
| :---: | :---: | :---: | :---: |
| *[Insert Image Here]* | *[Insert Image Here]* | *[Insert Image Here]* | *[Insert Image Here]* |

---

## 🚀 Installation & Setup

To run this project locally, follow these steps:

### 1. Clone the Repository
```bash
git clone https://github.com/VoidG4/CityVibe.git
```

### 2. Open in Android Studio
Open the cloned directory in **Android Studio** (Giraffe or newer recommended).

### 3. Firebase Setup (Crucial)
Because this project uses Firebase, you need to connect it to your own Firebase project:
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a new Android Project.
3. Register the app with the package name: `com.example.unipicityvibe`.
4. Add your computer's **SHA-1 Fingerprint** in the Firebase Project Settings (required for Google Play Services/Location to work in debug mode).
5. Download the `google-services.json` file.
6. Place the `google-services.json` file inside the `app/` directory of the cloned project.
7. Enable **Email/Password Authentication** in Firebase.
8. Set up **Firestore Database** and create two collections: `events` and `bookings`.

### 4. Build and Run
* Clean and Rebuild the project.
* Run the app on a physical device or an emulator (API Level 26+ recommended).
* *Note on Emulator:* For the Geolocation notifications to work, ensure you manually set the location in the Emulator's Extended Controls.

---

## 🗄️ Firestore Database Structure (Example)

**Collection:** `events`
```json
{
  "id": "event_1",
  "title": "Hamlet",
  "description": "Classic theater play...",
  "category": "theater",
  "date": "2025-06-20T21:00:00",
  "venue": "Municipal Theater",
  "price": 25.0,
  "availableTickets": 96,
  "imageUrl": "https://example.com/image.jpg",
  "lat": 37.9433,
  "lng": 23.6471
}
```

---
