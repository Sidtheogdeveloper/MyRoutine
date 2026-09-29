# MyRoutine

**Train. Track. Level up.**

MyRoutine is a native Android workout tracker for logging gym sessions, planning a weekly split, following strength progress, and earning rewards for consistency. It works offline and uses a dark charcoal interface with lime, purple, and orange accents.

The current app is written in **Java**, uses **Material components** for its interface, and stores workouts in **Room / SQLite**. It includes **397 bundled exercises** and five main destinations: **Home, Train, Progress, Quests, and You**.

## Contents

- [Features](#features)
- [Using the app](#using-the-app)
- [Gamification rules](#gamification-rules)
- [Progress calculations](#progress-calculations)
- [Technology and requirements](#technology-and-requirements)
- [Setup and installation](#setup-and-installation)
- [Project structure and architecture](#project-structure-and-architecture)
- [Storage and database migrations](#storage-and-database-migrations)
- [Export, sharing, and privacy](#export-sharing-and-privacy)
- [Exercise library](#exercise-library)
- [Testing and verification](#testing-and-verification)
- [Troubleshooting](#troubleshooting)
- [Current limitations and roadmap](#current-limitations-and-roadmap)
- [Contributing](#contributing)
- [License](#license)

## Features

### Home: your training dashboard

- Personalized greeting and current date.
- Athlete level and rank.
- Monday-to-Sunday training strip showing dates, today's highlight, and completed training days.
- Weekly session count against your chosen goal.
- Today's scheduled routine, a recovery message for a rest day, or a freestyle workout entry point.
- Resume action when an unfinished workout draft exists.
- Completed sets and lifted volume for the current week.
- XP progress toward the next level and an active-week streak.
- Three recent workout sessions with shortcuts to progress and quests.
- Daily hydration check-in with add/remove controls, 250 ml per glass, and an eight-glass progress display. This is a tracking display, not an individualized hydration prescription.

### Train: routines, presets, and exercises

- Start an empty freestyle workout.
- Start Push, Pull, Legs, or Full body preset sessions. Presets create three sets of eight reps per exercise; you can edit them before completing a set.
- Build a separate routine for each day of the week.
- Name routines and choose exercises, their order of addition, target sets, and target reps.
- Mark scheduled recovery days as rest days.
- Start today's routine with its exercises and targets already loaded.
- Browse the bundled exercise library and search by exercise name, primary muscle, or equipment.
- View primary and secondary muscles, movement pattern, equipment, movement type, and difficulty.
- Use a barbell plate calculator with an adjustable bar weight and total target load. It allocates 25, 20, 15, 10, 5, 2.5, and 1.25 kg plates on each side and reports any unallocated remainder. It assumes those plate sizes are available.

### Active workout: log every set

- Editable session name and workout notes.
- Running elapsed-time display and completed-set / volume summary.
- Add and remove exercises and sets.
- Log load in kilograms and whole-number repetitions; use **0 kg** for bodyweight movements.
- Complete or uncheck each set explicitly using the check control.
- Label sets as **Working, Warm-up, Drop set, or Failure**.
- Add optional **RPE** (rating of perceived exertion), accepted from 0 to 10; blank or 0 represents unused RPE.
- Show previous-session load and reps. New exercises and planned sessions can reuse the most recent recorded load; added sets copy the previous row's load and reps.
- Search the workout picker and hold a movement to add or remove a favorite. Favorites appear first when the picker is opened again.
- Rest timer options of **30, 60, 90, 120, 180, or 300 seconds**, with 90 seconds as the default.
- Automatically start rest after completing a set; tap an active timer to skip it.
- Haptic feedback on completed sets and the on-screen rest-complete event, subject to device support.
- Feedback when a completed load exceeds an existing best load for that exercise.
- Keep the screen awake while the workout activity is open.
- Save the session as a recoverable draft, including exercises, input values, completion flags, set types, RPE, notes, elapsed-time state, and the rest deadline.
- **Save & leave** pauses session time and clears the active rest countdown; returning resumes session time. Ordinary backgrounding does not explicitly pause elapsed time.
- Confirm before discarding a draft or replacing it with a new preset/freestyle session.
- Save completed sets and the parent workout together in one database transaction.
- Finish with a summary of XP, completed sets, lifted volume, and new load records.

Only checked sets are saved to workout history. At least one completed set is required to finish a session. Load and rep targets in a preset are suggestions to edit, not completed work.

### Progress: your training history

- Total recorded sessions and lifetime lifted volume.
- Daily volume chart for the current Monday-to-Sunday week.
- Best recorded load per exercise and estimated one-repetition maximum (1RM).
- Tap a record to view that exercise's performance across saved sessions.
- Weekly set distribution by **primary muscle group**.
- Journal of the latest 50 sessions, with names, dates, completed-set counts, duration, and volume.
- Session details including individual sets, set types, RPE, notes, and estimated calories.
- Repeat a previous session with its recorded exercises, loads, reps, and set types; completion flags start unchecked.
- Share a session using Android's share sheet.
- Hold a journal card to delete a session after confirmation; its linked sets are deleted as well.
- Open a calendar to inspect workouts on a selected date.

### Quests: rewards for consistency

- Workout XP and additional rewards for completed weekly quests.
- Levels, rank progression, and progress toward the next level.
- Three weekly quests with explicit claim buttons once their targets are met.
- Six milestone achievements in a trophy cabinet.
- Weekly consistency streaks that allow recovery days rather than requiring daily exercise.

### You: your athlete profile and data

- Athlete level and active-week streak.
- Weekly session goal selectable from **2 to 6 sessions**, with a default of 4.
- Physical profile: name, age, gender, height in centimeters, weight in kilograms, and optional body-fat percentage.
- Bodyweight check-ins with date and weight, a latest-entry display, and a history list.
- Workout CSV export through Android's document picker.

Bodyweight check-ins are stored separately from the physical profile. Logging a check-in does not automatically change the profile weight used for calorie estimates.

## Using the app

1. **Complete onboarding.** Enter your physical profile on the first launch; body-fat percentage is optional.
2. **Choose your weekly goal.** Open **You** to set the number of sessions you want to complete.
3. **Choose a session.** Start today's routine from **Home**, or use a preset or freestyle session from **Train**.
4. **Log and complete sets.** Enter load and reps, optionally add set type/RPE, and tap the check after performing each set.
5. **Rest between sets.** The rest timer starts automatically after a set is completed.
6. **Finish or pause.** Finish to save checked sets and earn XP, or choose **Save & leave** to resume the draft later.
7. **Review your progress.** Inspect volume, records, muscle distribution, and the workout journal under **Progress**.
8. **Claim quest rewards.** Open **Quests** after reaching a weekly target.
9. **Export your history.** Use **You ? Export workout history** and choose where to save the CSV.

Useful gestures: hold a movement in the workout picker to toggle its favorite status; hold a workout journal card to request deletion.

## Gamification rules

### XP and levels

A saved workout with at least one completed set earns:

```text
Workout XP = 100 + 10 ? min(completed sets, 40)
Level = 1 + floor(total XP / 500)
```

A workout earns at most **500 XP**. For example, one completed set earns 110 XP, and 12 completed sets earn 220 XP. Total XP includes workout XP calculated from current saved history plus previously claimed quest rewards.

Deleting a session removes its contribution to workout XP and statistics. Previously claimed quest XP remains stored. Set types are recorded as metadata; all saved set types contribute to current set counts, volume, and workout XP.

| Rank | Levels |
| --- | --- |
| Rookie | 1?4 |
| Contender | 5?9 |
| Challenger | 10?19 |
| Elite | 20?34 |
| Legend | 35 and above |

### Weekly quests

| Quest | Target within the current week | Reward |
| --- | --- | --- |
| Show up for yourself | Reach your selected weekly session goal | 150 XP |
| Rep by rep | Complete 40 sets | 100 XP |
| Move a mountain | Lift 10,000 kg of total volume | 100 XP |

Weeks begin at **Monday midnight in the device's local time zone**. Each quest reward can be claimed once for its week. Changing the weekly session goal changes the session quest's target and does not create an additional claim for an already claimed quest that week.

### Achievements and streaks

| Achievement | Requirement |
| --- | --- |
| First rep | Save 1 workout with recorded sets |
| Finding your rhythm | Complete 10 workouts |
| Iron regular | Complete 50 workouts |
| Century club | Log 100 completed sets |
| Heavy hitter | Accumulate 10,000 kg of lifetime volume |
| Built on consistency | Train in 4 consecutive weeks |

The active-week streak counts consecutive weeks containing at least one session with recorded sets. If you have not trained in the current week yet, the streak can still continue from the previous week. Achievements are evaluated from current history and the current active-week streak; they are not separately stored as permanent unlocks.

## Progress calculations

| Metric | Calculation / behavior |
| --- | --- |
| Set volume | `load in kg ? repetitions` |
| Session / weekly / lifetime volume | Sum of relevant saved set volumes |
| Bodyweight-set volume | 0 kg contributes 0 loaded volume; its reps and set count are still recorded |
| Best load | Highest recorded load for an exercise |
| Estimated 1RM | Actual load for 1 rep; otherwise `load ? (1 + reps / 30)` for 2?12 reps |
| Muscle distribution | Number of saved sets attributed to each exercise's primary muscle this week |
| Estimated calories | Weighted average exercise MET ? profile weight in kg ? session duration in hours |

Estimated 1RM is calculated only for positive loads and sets of 1?12 reps; other sets do not contribute an estimate. Calorie estimates use completed-set counts to weight each exercise's MET contribution, are rounded to one decimal internally, and use 70 kg if a profile is unavailable. MET values are inferred from exercise metadata in the bundled dataset. These are approximate training metrics, not sensor measurements.

## Technology and requirements

Versions below reflect this repository's configuration.

| Component | Version / setting |
| --- | --- |
| Platform | Native Android |
| Application ID / namespace | `com.example.myroutine` |
| App version | `1.0` (`versionCode = 1`) |
| Main source language | Java, source/target compatibility 11 |
| Build scripts | Gradle Kotlin DSL |
| Development JDK used for the verified build | JDK 21 |
| Gradle wrapper | 9.5.0 |
| Android Gradle Plugin | 9.2.0 |
| Compile / target SDK | API 37 |
| Minimum SDK | API 24 / Android 7.0 |
| Material components | 1.14.0 |
| AppCompat | 1.8.0 |
| AndroidX Activity | 1.13.0 |
| Room | 2.6.1, with Java annotation processing |
| Lifecycle ViewModel / LiveData | 2.8.7 |
| RecyclerView | 1.3.2 |
| ConstraintLayout | 2.1.4 |
| Unit tests | JUnit 4.13.2 |
| Device tests | AndroidX Test JUnit 1.3.0 and Espresso 3.7.0 |

Development requires Android Studio or the Android command-line SDK tools, SDK Platform 37, a suitable JDK, and an API 24+ emulator or physical Android device. Initial dependency resolution needs access to Google Maven, Maven Central, and the Gradle distribution host. Normal workout tracking needs no account or application backend.

**iOS:** the current Java/Android project and APK cannot be installed on an iPhone. Supporting iOS requires an iOS implementation or a migration to a framework supporting both platforms.

## Setup and installation

### Open in Android Studio

1. Clone or download this repository and open its root directory in Android Studio.
2. Install **Android SDK Platform 37** through SDK Manager.
3. Configure the Gradle JDK; JDK 21 was used for the recorded successful build.
4. Allow Gradle to sync dependencies.
5. Select an API 24+ emulator or connect an Android device with USB debugging enabled.
6. Run the **app** configuration. The launcher opens onboarding and then the main navigation host.

Android Studio normally generates `local.properties` with your SDK location. For command-line development, configure that file or `ANDROID_HOME` for your local SDK. Keep machine-specific SDK paths out of version control.

### Build a debug APK

From the project root on Windows / PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

On macOS / Linux:

```bash
chmod +x gradlew
./gradlew assembleDebug
```

Output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Install on Android

With `adb` available on your `PATH` and a device connected:

```powershell
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.myroutine/.ui.onboarding.OnboardingActivity
```

Alternatively, transfer the debug APK to an Android device and open it, allowing installation from that source when Android prompts. Installing an update over an existing app requires a compatible signing key. The debug APK is a development artifact.

### Release builds

```powershell
.\gradlew.bat assembleRelease
```

No release signing configuration is included. Configure your own release signing before distributing a production build. Release optimization is currently disabled in `app/build.gradle.kts`.

## Project structure and architecture

```text
MyRoutine/
??? app/
?   ??? build.gradle.kts                # Android settings and dependencies
?   ??? src/
?       ??? main/
?       ?   ??? AndroidManifest.xml
?       ?   ??? assets/exercises.csv    # Bundled exercise catalog
?       ?   ??? java/com/example/myroutine/
?       ?   ?   ??? MainActivity.java   # Main navigation host
?       ?   ?   ??? data/
?       ?   ?   ?   ??? AppDatabase.java
?       ?   ?   ?   ??? WorkoutRepository.java
?       ?   ?   ?   ??? dao/            # Room queries and writes
?       ?   ?   ?   ??? entity/         # Six persisted entity types
?       ?   ?   ??? ui/
?       ?   ?   ?   ??? ForgeUi.java    # Shared colors and native UI builders
?       ?   ?   ?   ??? TrainingHubFragment.java
?       ?   ?   ?   ??? onboarding/
?       ?   ?   ?   ??? workout/
?       ?   ?   ?   ??? routines/
?       ?   ?   ?   ??? profile/
?       ?   ?   ?   ??? calendar/
?       ?   ?   ?   ??? dashboard/     # Earlier dashboard implementation
?       ?   ?   ?   ??? home/          # Workout list adapter
?       ?   ?   ??? util/              # Statistics, CSV parser, calorie estimates
?       ?   ??? res/                   # Layouts, themes, icons, menus, strings
?       ??? test/                      # JVM unit tests
?       ??? androidTest/               # Device instrumentation tests
??? gradle/libs.versions.toml           # Dependency version catalog
??? gradle/wrapper/                     # Pinned Gradle distribution
??? build.gradle.kts
??? settings.gradle.kts
??? gradle.properties
??? gradlew / gradlew.bat
??? README.md
```

| Area | Main implementation |
| --- | --- |
| First-launch profile and routing | [OnboardingActivity](app/src/main/java/com/example/myroutine/ui/onboarding/OnboardingActivity.java), [MainActivity](app/src/main/java/com/example/myroutine/MainActivity.java) |
| Five main destinations | [TrainingHubFragment](app/src/main/java/com/example/myroutine/ui/TrainingHubFragment.java) |
| Shared visual components and insets | [ForgeUi](app/src/main/java/com/example/myroutine/ui/ForgeUi.java) |
| Active session UI and recoverable state | [ActiveWorkoutActivity](app/src/main/java/com/example/myroutine/ui/workout/ActiveWorkoutActivity.java), [ActiveWorkoutViewModel](app/src/main/java/com/example/myroutine/ui/workout/ActiveWorkoutViewModel.java) |
| Weekly routine editing | [RoutineDetailActivity](app/src/main/java/com/example/myroutine/ui/routines/RoutineDetailActivity.java) |
| Database access | [AppDatabase](app/src/main/java/com/example/myroutine/data/AppDatabase.java), [WorkoutRepository](app/src/main/java/com/example/myroutine/data/WorkoutRepository.java), Room DAOs |
| XP, volume, PRs, and streaks | [TrainingStats](app/src/main/java/com/example/myroutine/util/TrainingStats.java) |
| Catalog import and MET values | [ExerciseCsvParser](app/src/main/java/com/example/myroutine/util/ExerciseCsvParser.java) |
| Calorie estimates | [CalorieCalculator](app/src/main/java/com/example/myroutine/util/CalorieCalculator.java) |

Activities and fragments observe LiveData to display persisted changes. The active session uses an AndroidViewModel and JSON drafts in SharedPreferences. Other existing screens use ViewModels and `WorkoutRepository`; the current training hub and active-session implementation also access Room directly. Database writes run on a shared executor. The UI combines programmatically built native views with XML layouts for onboarding, profile editing, routines, and calendar screens.

`TrainingHubFragment` is the current five-tab experience. Earlier dashboard/routines fragments and workout adapters remain in the source tree; their presence does not imply additional main navigation tabs.

## Storage and database migrations

The Room database is named **`myroutine_database`** and is currently at **schema version 4**.

| Table | Stored data |
| --- | --- |
| `exercises` | Names, muscles, movement patterns, equipment, unilateral flag, movement type, difficulty, and inferred MET |
| `workouts` | Session name, start timestamp, duration, estimated calories, and notes |
| `workout_sets` | Linked workout/exercise IDs, reps, load, set type, and optional RPE |
| `user_profile` | Singleton physical profile and creation/update timestamps |
| `weekly_routines` | Day of week, routine name, rest flag, and effective-from timestamp |
| `routine_exercises` | Linked routine/exercise IDs, set/rep targets, and exercise order |

Foreign keys link workout sets and routine exercises to their parent records. Removing a workout cascades to its sets. Routine edits create a new effective version, and the app uses the most recent eligible routine for each day.

SharedPreferences stores:

- `myroutine_prefs`: onboarding completion.
- `training`: weekly goal, rest duration, favorites, claimed quest rewards, hydration entries, and bodyweight entries.
- `workout_draft`: the unfinished session serialized as JSON.

The explicit **v3 ? v4 migration** adds workout notes, set type, and RPE while preserving existing records. Defaults for migrated rows are empty notes, `Working` set type, and RPE 0. No v1/v2 migration path is currently registered. Future schema changes need an additional migration; export schemas are currently disabled in Room's configuration.

The exercise catalog is seeded when the database is created or found empty. Seeding checks the row count inside a transaction to avoid duplicate imports. Saving a completed workout or edited routine writes its parent and linked rows atomically. A failed session save retains the draft for retry.

## Export, sharing, and privacy

### Workout CSV

Export creates a UTF-8 CSV through Android's document picker. You choose the destination, which may be local storage or a document provider available on the device.

Each saved set becomes one row with these columns:

```csv
workout_id,date,workout,exercise,weight_kg,reps,set_type,rpe,notes
```

Dates use local `yyyy-MM-dd HH:mm` formatting. Text fields are quoted and embedded quotes are escaped. Leading spreadsheet-formula characters in text fields are prefixed with an apostrophe.

The export includes all stored workout sets, including sessions outside the 50-entry journal display. It does **not** contain routines, the physical profile, hydration, bodyweight check-ins, favorites, or quest claims. CSV import/full-state restore is not implemented.

### Sharing and device storage

Session sharing opens Android's share sheet with a text summary. The selected receiving app controls what happens to that shared text.

MyRoutine has no account system, application backend, or app-level cloud synchronization. The manifest declares no network or broad-storage permissions. Workouts are stored in the app's private database; draft and habit data use private preferences.

**Android backup is enabled in the manifest.** The backup-rule files currently use platform defaults, so operating-system backup or device-transfer behavior can apply depending on the device and its settings. Offline operation should not be interpreted as a guarantee that Android will never back up app data. Uninstalling or clearing storage removes the local copy; workout CSV export is the app's explicit export option.

## Exercise library

The catalog lives in [app/src/main/assets/exercises.csv](app/src/main/assets/exercises.csv). Its current header is:

```csv
exercise,primary_muscle,secondary_muscles,movement_pattern,equipment,unilateral,compound_or_isolation,difficulty
```

Rows include primary and secondary muscle information, movement patterns, equipment, unilateral flags, Compound/Isolation/Isometric classification, and difficulty. The parser calculates MET values from that metadata and supplies five fallback exercises if no valid catalog entries can be loaded.

To extend the catalog:

1. Add rows with all eight fields and retain the header.
2. Use semicolons for multiple values within a field, as in `Front Delts;Triceps`.
3. Keep fields free of commas: the current parser splits on commas and does not implement quoted CSV-field parsing.
4. Keep preset exercise names compatible with the preset lookups in `ActiveWorkoutViewModel`.
5. Plan a catalog update for existing installations. Editing the asset affects fresh/empty databases; an already populated database is not automatically replaced.

Custom exercise creation from the app is not currently available.

## Testing and verification

The previously recorded debug build passed **9 unit tests** and **5 device tests**. This is the last recorded verification, not a claim that tests rerun automatically when documentation changes.

| Suite | Coverage |
| --- | --- |
| `TrainingStatsTest` | Empty histories, actual volume and records, weekly boundaries, exclusion of other weeks from current-week totals, orphan/empty-session XP behavior, bodyweight sets, weekly streaks, and estimated-1RM / XP bounds |
| `TrainingDatabaseTest` | Preserving workouts and sets during the v3 ? v4 migration; rolling back the whole session after a failed set insertion |
| `WorkoutDraftTest` | Recovering completed sets, notes, set type, and RPE; keeping discarded drafts removed; preserving paused elapsed time across ViewModel recreation |
| Existing example tests | JVM smoke test and application-context check |

Recorded emulator checks covered the five destinations, Push preset, completing a 60 kg ? 8 set, forced process-stop recovery, and the expected 480 kg volume / 110 XP summary. The session created for that check was removed afterward.

### Standard commands

On Windows:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug assembleDebugAndroidTest
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat lintDebug
```

On macOS/Linux, use `./gradlew` with the same task names. Device tests require a connected emulator or Android device.

### Direct device runner

If Gradle's device runner cannot resolve its own dependencies, build both APKs and use ADB:

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.example.myroutine.test/androidx.test.runner.AndroidJUnitRunner
```

The recorded verification used this direct runner. **Lint did not complete** in that environment because required dependencies were unavailable and Google's Maven host could not be resolved. This is an environment limitation, not a successful lint result.

## Troubleshooting

| Problem | What to check |
| --- | --- |
| Gradle cannot find Java | Configure Android Studio's Gradle JDK or `JAVA_HOME` for command-line builds |
| SDK location / API 37 errors | Install SDK Platform 37 and configure `local.properties` or `ANDROID_HOME` |
| Dependency download / DNS failure | Check access to Google Maven, Maven Central, and Gradle's distribution host; `--offline` works only for dependencies already cached |
| Configuration-cache failure | Retry the relevant Gradle task with `--no-configuration-cache` |
| Device absent from `adb devices` | Enable USB debugging, confirm the device authorization prompt, or start an emulator |
| APK update rejected for signing mismatch | Use the original signing key; uninstalling an existing app to change keys removes its local data |
| Workout cannot finish | Complete at least one valid set; unchecked sets are excluded from history |
| Workout inputs rejected | Use whole reps from 1?1000, finite load from 0?10,000 kg, and optional RPE from 0?10 |
| Changed CSV does not appear in an existing installation | The seed only fills an empty exercise table; implement a catalog update/migration rather than assuming an asset edit refreshes existing rows |
| Older database cannot open | Only the v3 ? v4 migration is registered; add a supported migration for the older schema |

The numeric input bounds are application validation limits, not suggested training loads or repetition targets.

## Current limitations and roadmap

These are potential improvements, not implemented features:

- iOS support through a separate implementation or a framework migration.
- Superset grouping, automatic warm-up suggestions, and progressive-overload targets.
- Long-term strength trend charts and richer comparisons across training cycles. Current exercise history is a list; the volume chart covers the current week.
- Background rest-completion notifications. The countdown deadline survives backgrounding, but alerts are handled in the open workout activity; no notification service is configured.
- Custom exercises, timed/cardio set fields, distance tracking, and kg/lb switching. Current set logging uses kg and repetitions.
- Custom plate inventories and smaller plate sizes in the plate calculator.
- Full backup import/restore, cloud synchronization, and accounts.
- Social leaderboards, shared challenges, and wearable/health-platform integrations.
- Nutrition/food tracking, guided exercise videos, recovery scoring, and automated coaching.
- Broader localization, accessibility review, and verification across additional device sizes.

Other current display limits: the exercise browser and workout picker show up to 80 matching movements at a time, and the main PR view shows up to 12 weighted exercise records. Narrow a library search to find additional matches. The workout journal shows 50 recent sessions, while the calendar and CSV export offer additional ways to access history.

## Contributing

Keep changes focused and document behavior that affects logged data, statistics, or rewards. Start with the source files listed in the architecture table. Update the version catalog for dependency changes and add a Room migration for database schema changes.

For changes to persistence or calculations, run the relevant unit/device checks described above. Avoid committing local SDK paths, signing keys, generated build output, or private workout exports. Update this README when a feature's behavior or a setup requirement changes.

## License

No project `LICENSE` file is currently included. This README does not assign a license to the source code or bundled exercise dataset.
