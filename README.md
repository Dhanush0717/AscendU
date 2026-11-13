# AscendU 🚀

> **Your Days. Your Discipline. Your Transformation.**

AscendU is an offline-first, native Android personal productivity and discipline planner designed to help students and professionals build consistent daily routines, execute high-priority tasks, maintain accountable habits, and visualize long-term progress.

---

## 📱 Features

- **🌅 Today Dashboard**: Chronological timeline of daily routine commitments, quick-toggle completions, and daily summary statistics.
- **📋 Plan Module**: Comprehensive management for both recurring commitments (habits with custom frequency & time of day) and one-time prioritized tasks.
- **📈 My Arc Analytics**: Monthly consistency grid, historical trend analysis, streak counters (current and longest), and manual history correction.
- **🔔 Notifications & Accountability**: Morning routine reminders and evening accountability summaries powered by `AlarmManager` and `BroadcastReceiver` (with device reboot recovery).
- **🎨 Modern Design System**: Clean, responsive Material 3 UI with elevated neumorphic cards, semantic color states, and custom typography (**Inter** font family) with full Light & Dark mode support.
- **🔒 100% Offline-First**: All data is stored locally using an encrypted Room SQLite database—no accounts, no cloud sync, complete privacy.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Java 17
- **UI Toolkit**: Native XML Layouts, Material Design 3 Components, ViewBinding
- **Architecture**: MVVM (Model - View - ViewModel) + Repository Pattern
- **Local Persistence**: Android Jetpack Room (SQLite ORM) with LiveData observers
- **Navigation**: Jetpack Navigation Component with Single-Activity (`MainActivity`) + Fragment architecture
- **Background Tasks**: `AlarmManager` (Exact Alarms) + `BroadcastReceiver` + `NotificationCompat`
- **Preferences**: `SharedPreferences` for user onboarding and notification preferences
- **Target SDK**: Android 14 (API 34) | **Min SDK**: API 26

---

## 📂 Project Structure

```
com.ascendu.app/
├── data/
│   ├── local/
│   │   ├── dao/         # Room DAOs (TaskDao, RecurringTaskDao, etc.)
│   │   ├── database/    # AppDatabase & TypeConverters
│   │   └── entity/      # Room SQLite Entities (@Entity)
│   ├── model/           # POJO data models for analytics & stats
│   └── repository/      # Repository layer managing background threads & database ops
├── notifications/       # Alarm schedulers, notification builders & BroadcastReceivers
├── ui/
│   ├── arc/             # My Arc analytics, consistency grid, and history correction
│   ├── onboarding/      # Multi-step onboarding flow (Welcome, Profile, Habits, Schedule)
│   ├── plan/            # Plan module (Habits & One-Time Tasks management)
│   ├── settings/        # Notification & reminder preferences
│   └── today/           # Today dashboard & chronological timeline
└── MainActivity.java    # Single-activity container with Navigation Component & BottomNav
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**
- **JDK**: Java 17
- **Android SDK**: API 34 (Android 14)

### Installation & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/<your-repo-name>.git
   cd AscendU
   ```
2. Open the project folder in **Android Studio**.
3. Let Gradle sync dependencies automatically.
4. Select an Android Emulator (Pixel 6 / API 34 recommended) or a physical Android device with USB debugging enabled.
5. Click **Run (`Shift + F10`)**.

---

## 📜 License
This project is developed as an academic and personal portfolio demonstration. All rights reserved.
