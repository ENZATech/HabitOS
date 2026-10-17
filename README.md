# habitOS 📋

> **100% Native Android App** built with Kotlin + Jetpack Compose  
> Matte Obsidian Dark Theme · Fully Offline · AlarmManager Notifications

[![Build habitOS APK](https://github.com/YOUR_USERNAME/HabitOS/actions/workflows/build.yml/badge.svg)](https://github.com/YOUR_USERNAME/HabitOS/actions/workflows/build.yml)

## Features

- ☀️ **My Day** — Daily task view with progress, suggestions, and task detail sheet with subtasks & notes
- 🔥 **7-Day Habit Matrix** — Mon–Sun check-in grid, streak counters, Perfect Day 👑 detection, haptic vibration
- 📅 **Planned** — Overdue / Today / Upcoming task grouping
- 🎂 **Birthdays** — Today / Yesterday-missed / Upcoming with 1-tap WhatsApp wish generator
- 📚 **Lists** — Custom color-tagged lists + **Clear Data Vault** (PIN + `CLEAR` confirmation)
- 🔔 **Native Notifications** — AlarmManager EOD reminder (21:00, configurable) + birthday alerts (midnight + day before)
- ↖️ **Undo Delete** — 5-second undo snackbar on every delete action

## Building

### Via GitHub Actions (Recommended)
1. Push this repo to GitHub
2. GitHub Actions automatically builds the APK
3. Download from the **Actions** tab → latest workflow run → **Artifacts**

### Locally
```bash
# Make sure Android SDK is installed and ANDROID_HOME is set
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Tech Stack

| Component | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose 2.8.5 |
| Database | Room 2.6.1 |
| Preferences | DataStore 1.1.1 |
| Background | AlarmManager + BroadcastReceiver |
| DI | Manual (HabitOSApp) |

## Adaptive Icon

The launcher icon uses the Android Adaptive Icon spec:
- **Background**: `#0C0A09` (obsidian)
- **Foreground**: Emerald ring + checkmark in the **66dp safe zone**
- No cropping on any Android launcher

## Clear Data Vault

Go to **Lists tab → 🔒 icon** (top right):  
Enter PIN (`1234` default) + type `CLEAR` → all data is wiped for a fresh start.  
You can change the PIN from the same dialog.

---
*Built with ❤️ using Kotlin + Jetpack Compose*
