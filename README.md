# 🧠 Mental Health Companion App

A powerful **AI-powered mental wellness Android application** designed to help users reflect, analyze, and improve their mental state. Built using **Kotlin**, **Firebase**, **ML Kit**, and **OpenAI's GPT**, this app combines the power of real-time facial emotion detection, AI chat support, and meditation guidance to promote mental well-being.

---

## 🌟 Features

### 👤 1. User Authentication
- Firebase-based **email/password login**
- **Secure registration & password reset** functionality
- New users can sign up, returning users can log in seamlessly

### 🧠 2. Mood Analysis
- Users can **enter a written reflection** about how they feel
- Users can also **use the camera (via ML Kit Face Detection)** to analyze facial expressions and detect **mood scores**
- The app combines **text + face data** to generate a mental health insight score (0–100)

### 🤖 3. AI-Powered Chat Support
- An **OpenAI GPT-3.5-powered chatbot** provides personalized support
- Based on the user's mood score and reflection, the chatbot suggests:
  - Talking to a therapist
  - Practicing gratitude
  - Starting a breathing/meditation session
  - Taking a mindful break

### 🎵 4. Guided Meditation
- Starts a **30-second calm music session** with a visual timer
- Automatically plays background music and displays a breathing animation
- Reminds users to close their eyes, focus, and breathe
- Post-meditation, the AI provides a **personalized message** based on the user's reflection

### 🔁 5. Daily Reminders
- Users receive **daily push reminders** to reflect and meditate
- Helps maintain consistency and build healthy habits

### 📊 6. Post-Meditation Feedback
- After each session, the app analyzes:
  - User's reflection (if given)
  - Session data
- AI gives supportive feedback and encouragement
- Returns a **“meditationEffect” score** based on positivity of response

---

## 📷 Technologies Used

| Feature                   | Tech/Library                     |
|---------------------------|----------------------------------|
| Frontend                  | Kotlin + Android XML             |
| Backend                   | Firebase Auth, Firestore         |
| Mood Detection            | Google ML Kit - Face Detection   |
| AI Chat                   | OpenAI GPT-3.5 (Chat Completions)|
| Meditation                | MediaPlayer, ProgressBar         |
| Notifications             | WorkManager + AlarmManager       |
| Secure Key Storage        | EncryptedSharedPreferences       |


🔒 Security & Privacy

- **OpenAI API Key** is securely stored using `EncryptedSharedPreferences`
- **User data (mood scores, reflections)** is stored securely in **Firebase Firestore**
- Password reset functionality is supported for safe recovery
- Reflection data is analyzed locally unless user agrees to sync with cloud
