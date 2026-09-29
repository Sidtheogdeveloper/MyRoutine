# MyRoutine

A native, offline Android training app built in Java with Material components, ViewModels and Room. Dark charcoal, lime accents, custom navigation icons and five connected destinations: Home, Train, Progress, Quests and You.

## Included

- Workout logging with weight in kg, reps, explicit set completion, warm-up / working / drop / failure set types, optional RPE, session names and notes.
- Previous-session values, best-load celebrations, configurable rest timers, haptic set feedback and repeatable workout history.
- Recoverable drafts after app restarts. Save & leave pauses elapsed session time; resuming continues it. Discarded sessions stay discarded.
- Weekly routine editor with target sets and reps, scheduled rest days, and Push / Pull / Legs / Full body quick starts.
- Searchable exercise library with equipment, muscle and movement metadata. Hold an exercise in the workout picker to toggle a favorite.
- Weekly volume chart, lifetime volume, per-exercise load records, estimated 1RM, exercise history, muscle set distribution and a calendar journal.
- XP, athlete ranks, levels, six milestone achievements and three weekly quests with rewards claimable once per week. Weekly consistency streaks allow rest days.
- Adjustable weekly goals, daily hydration logging, bodyweight check-ins and a physical profile.
- CSV export through Android's document picker, session sharing, and confirmed history deletion (hold a journal card).

All visible training statistics come from recorded sets. Saving a workout or routine writes its parent and child rows in one transaction. The Room v3 → v4 migration preserves existing data and adds set type, RPE and notes. Exercise seeding is idempotent.

## Run

Open the project in Android Studio with JDK 21 and Android SDK 37. Minimum supported Android version is 7.0 (API 24).

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat lintDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

The app stores data on the device. Cloud sync, social leaderboards, wearable integration, food databases, and guided exercise videos are not connected services in this build. CSV exports contain workout sets; bodyweight and hydration remain local. Estimated 1RM is shown for weighted sets of 1–12 reps; calorie values are labeled as estimates.

## Verification

The final debug build passes 9 unit tests and 5 device tests.

Unit coverage includes volume, XP, records, week boundaries, recovery-friendly streaks and empty histories. Device tests cover the v3 migration, transaction rollback, draft restoration and paused-session timing. Emulator QA also checks the five tabs, quick starts, completing a set, process-stop recovery, and the XP summary.

This environment cannot resolve Google's Maven host for the missing lint / Gradle device-runner dependencies. The APK and test APK can still be built from cached dependencies; device tests can be executed directly:

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.example.myroutine.test/androidx.test.runner.AndroidJUnitRunner
```
