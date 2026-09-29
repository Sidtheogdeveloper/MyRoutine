# MyRoutine V2 — Gamified Dashboard, Calendar, Scheduling & Stats

Major expansion of MyRoutine with gamified UI, bottom navigation, weekly routine scheduling, calendar history, user profile, and calorie tracking.

## Proposed Changes — 6 Phases

---

### Phase 1: Navigation Overhaul

Convert the single-Activity architecture into a **Bottom Navigation** pattern with 4 tabs. `MainActivity` becomes the navigation host.

```
┌──────────────────────────────────────────┐
│           MaterialToolbar                │
├──────────────────────────────────────────┤
│                                          │
│          Fragment Content Area            │
│                                          │
├──────────────────────────────────────────┤
│  🏠 Dashboard  📅 Calendar  📋 Routines  👤 Profile │
└──────────────────────────────────────────┘
```

#### [MODIFY] [MainActivity.java](file:///c:/Users/siddh/Siddharth/Code%20Files/MyRoutine/app/src/main/java/com/example/myroutine/MainActivity.java)
Rewrite as a navigation host with BottomNavigationView that swaps between 4 fragments.

#### [MODIFY] [activity_main.xml](file:///c:/Users/siddh/Siddharth/Code%20Files/MyRoutine/app/src/main/res/layout/activity_main.xml)
Replace current layout with FrameLayout container + BottomNavigationView.

#### [NEW] `res/menu/bottom_nav_menu.xml`
Menu resource defining the 4 tabs: Dashboard, Calendar, Routines, Profile.

#### [NEW] `res/drawable/` — Bottom nav icons
Custom vector drawables for each tab icon (`ic_dashboard.xml`, `ic_calendar.xml`, `ic_routines.xml`, `ic_profile.xml`).

---

### Phase 2: User Profile & Onboarding

Collect user details on first launch to enable calorie calculation and personalization.

#### New Entity: `UserProfile`

| Field | Type | Notes |
|---|---|---|
| `id` | int (PK) | Always row 1 (singleton) |
| `name` | String | Display name |
| `age` | int | Years |
| `gender` | String | "Male" / "Female" / "Other" |
| `heightCm` | double | Height in centimeters |
| `weightKg` | double | Weight in kilograms |
| `bodyFatPercentage` | double | Optional (default -1 = not set) |
| `createdAt` | long | Timestamp |
| `updatedAt` | long | Timestamp |

#### Files

| File | Path | Description |
|---|---|---|
| [NEW] `UserProfile.java` | `data/entity/` | Room entity |
| [NEW] `UserProfileDao.java` | `data/dao/` | Insert, update, getProfile |
| [NEW] `OnboardingActivity.java` | `ui/onboarding/` | First-launch wizard: name → age/gender → height/weight → optional body fat |
| [NEW] `activity_onboarding.xml` | `res/layout/` | Step-by-step onboarding UI with animated transitions |
| [NEW] `ProfileFragment.java` | `ui/profile/` | View & edit profile details, Bottom Nav tab 4 |
| [NEW] `fragment_profile.xml` | `res/layout/` | Profile display with edit fields |
| [NEW] `ProfileViewModel.java` | `ui/profile/` | LiveData for profile |

#### Onboarding Flow
- On launch, check if `UserProfile` exists.
- If not → redirect to `OnboardingActivity` (blocks access until complete).
- Stored in SharedPreferences: `onboarding_complete = true`.

---

### Phase 3: Weekly Routine Scheduling

Users define a recurring weekly plan. Each day of the week has a named routine with a list of exercises. The plan repeats every week; modifications apply to future sessions only.

#### New Entity: `WeeklyRoutine`

| Field | Type | Notes |
|---|---|---|
| `id` | int (PK) | Auto-gen |
| `dayOfWeek` | int | 1=Monday … 7=Sunday |
| `routineName` | String | e.g. "Push Day", "Legs", "Rest" |
| `isRestDay` | boolean | If true, no exercises |
| `effectiveFrom` | long | Timestamp — allows versioning for "changes apply to future only" |

#### New Entity: `RoutineExercise`

| Field | Type | Notes |
|---|---|---|
| `id` | long (PK) | Auto-gen |
| `routineId` | int (FK → WeeklyRoutine) | Cascade delete |
| `exerciseId` | int (FK → Exercise) | |
| `targetSets` | int | Suggested number of sets |
| `targetReps` | int | Suggested reps per set |
| `orderIndex` | int | Display order within routine |

#### Files

| File | Path | Description |
|---|---|---|
| [NEW] `WeeklyRoutine.java` | `data/entity/` | Entity |
| [NEW] `RoutineExercise.java` | `data/entity/` | Entity with FK to routine + exercise |
| [NEW] `WeeklyRoutineDao.java` | `data/dao/` | CRUD, getByDayOfWeek, getActiveForDay |
| [NEW] `RoutineExerciseDao.java` | `data/dao/` | CRUD, getExercisesForRoutine |
| [NEW] `RoutinesFragment.java` | `ui/routines/` | Bottom Nav tab 3 — weekly grid view (Mon–Sun) |
| [NEW] `fragment_routines.xml` | `res/layout/` | 7-day card grid, tap to edit |
| [NEW] `RoutineDetailActivity.java` | `ui/routines/` | Edit a single day's routine: name, add/remove exercises, set targets |
| [NEW] `activity_routine_detail.xml` | `res/layout/` | Detail editor |
| [NEW] `RoutinesViewModel.java` | `ui/routines/` | LiveData for weekly routines |
| [NEW] `item_day_card.xml` | `res/layout/` | Card for each day of the week |
| [NEW] `item_routine_exercise.xml` | `res/layout/` | Exercise row in routine editor |
| [NEW] `RoutineExerciseAdapter.java` | `ui/routines/` | Adapter for routine exercise list |

#### "Changes apply to future" logic
When the user edits a routine, a new `WeeklyRoutine` row is created with `effectiveFrom = now`. Queries for "today's routine" use: `WHERE dayOfWeek = :today AND effectiveFrom <= :now ORDER BY effectiveFrom DESC LIMIT 1`.

---

### Phase 4: Gamified Dashboard

The Dashboard (tab 1) becomes the main screen with at-a-glance stats, today's routine, streaks, and PR highlights.

#### Dashboard Sections

```
┌─────────────────────────────────────┐
│  👋 Good Morning, {name}!          │
│  🔥 12-day streak                   │
├─────────────────────────────────────┤
│  TODAY'S ROUTINE: Push Day          │
│  ┌─────────────────────────────────┐│
│  │ Bench Press    3×10             ││
│  │ Incline DB     3×12             ││
│  │ Cable Fly      3×15             ││
│  └─────────────────────────────────┘│
│       [START TODAY'S WORKOUT]       │
├─────────────────────────────────────┤
│  📊 STATS                          │
│  ┌──────┐ ┌──────┐ ┌──────┐       │
│  │ 342  │ │ 85kg │ │ 24   │       │
│  │ cal  │ │ PR   │ │ sets │       │
│  │ last │ │Bench │ │ this │       │
│  │ sess │ │ PRs  │ │ week │       │
│  └──────┘ └──────┘ └──────┘       │
├─────────────────────────────────────┤
│  RECENT WORKOUTS                    │
│  ┌─────────────────────────────────┐│
│  │ Push Day — Sep 14  │ 342 cal   ││
│  │ Pull Day — Sep 13  │ 298 cal   ││
│  └─────────────────────────────────┘│
└─────────────────────────────────────┘
```

#### Files

| File | Path | Description |
|---|---|---|
| [NEW] `DashboardFragment.java` | `ui/dashboard/` | Bottom Nav tab 1 — the main gamified screen |
| [NEW] `fragment_dashboard.xml` | `res/layout/` | Full dashboard layout with all sections |
| [NEW] `DashboardViewModel.java` | `ui/dashboard/` | Aggregates: streak, today's routine, stats, recent workouts |
| [NEW] `item_dashboard_stat.xml` | `res/layout/` | Stat card (calories, PR, sets this week) |
| [NEW] `item_today_exercise.xml` | `res/layout/` | Exercise preview row for today's routine |
| [NEW] `TodayExerciseAdapter.java` | `ui/dashboard/` | Adapter for today's exercise preview list |

#### Streak Calculation
Count consecutive days with at least one workout, working backwards from today. Derived from `Workout.timestamp` — no new entity needed. Computed in the ViewModel.

#### PR Detection
Query: `SELECT MAX(weight) FROM workout_sets WHERE exerciseId = :id` per exercise. Compare with previous best. The dashboard highlights PRs for today's muscle group.

---

### Phase 5: Calendar View

A full-screen calendar (tab 2) that marks workout days and lets the user tap any date to see what they did.

#### Files

| File | Path | Description |
|---|---|---|
| [NEW] `CalendarFragment.java` | `ui/calendar/` | Bottom Nav tab 2 — CalendarView + day detail |
| [NEW] `fragment_calendar.xml` | `res/layout/` | CalendarView on top, RecyclerView below for selected day's workouts |
| [NEW] `CalendarViewModel.java` | `ui/calendar/` | Queries workouts by date range, selected date |
| [NEW] `CalendarDayAdapter.java` | `ui/calendar/` | Adapter for the day-detail workout list |
| [NEW] `item_calendar_workout.xml` | `res/layout/` | Workout summary card for a selected day |

#### Query Strategy
- `getWorkoutsBetween(startMs, endMs)` — for marking dots on calendar days.
- `getWorkoutsForDate(startOfDayMs, endOfDayMs)` — for showing detail when a day is tapped.

---

### Phase 6: Calorie Tracking & Stats Engine

#### Calorie Calculation: MET-based formula

```
Calories = MET × weightKg × durationHours
```

Each exercise gets a MET (Metabolic Equivalent of Task) value:

| Exercise Category | MET Value |
|---|---|
| Heavy compound lifts (Squat, Deadlift, Bench) | 6.0 |
| Moderate compound (Rows, OHP, Leg Press) | 5.0 |
| Isolation exercises (Curls, Flys, Raises) | 3.5 |
| Core / Bodyweight (Plank, Crunches) | 3.0 |

#### Entity Changes

##### [MODIFY] `Exercise.java` — add `metValue` field (double)

##### [MODIFY] `Workout.java` — add:
- `durationSeconds` (long) — actual workout duration captured from the chronometer
- `caloriesBurnt` (double) — calculated at save time

#### New Files

| File | Path | Description |
|---|---|---|
| [NEW] `CalorieCalculator.java` | `util/` | Static utility: `calculateCalories(List<ExerciseWithSets>, userWeightKg, durationSeconds)` |

#### Integration
When `finishWorkout()` is called in `ActiveWorkoutViewModel`:
1. Capture chronometer duration → `durationSeconds`.
2. Fetch user's weight from `UserProfile`.
3. Call `CalorieCalculator.calculateCalories(...)`.
4. Store result in `Workout.caloriesBurnt` before inserting.

---

## Database Migration Strategy

Since V1 hasn't been released to users, we'll use **`fallbackToDestructiveMigration()`** to wipe and recreate the database with the new schema. This avoids complex migration scripts during development.

> [!WARNING]
> This will erase any test data from V1. If you've been testing and want to preserve data, let me know and I'll write proper migration SQL instead.

#### [MODIFY] [AppDatabase.java](file:///c:/Users/siddh/Siddharth/Code%20Files/MyRoutine/app/src/main/java/com/example/myroutine/data/AppDatabase.java)
- Bump to `version = 2`
- Add new entities: `UserProfile`, `WeeklyRoutine`, `RoutineExercise`
- Add new DAOs
- Update exercise seed data with MET values
- Add `fallbackToDestructiveMigration()`

---

## Full File Tree (New & Modified)

```
app/src/main/java/com/example/myroutine/
├── MainActivity.java                           (MODIFY — nav host)
├── data/
│   ├── AppDatabase.java                        (MODIFY — v2 + new entities)
│   ├── WorkoutRepository.java                  (MODIFY — new methods)
│   ├── entity/
│   │   ├── Exercise.java                       (MODIFY — add metValue)
│   │   ├── Workout.java                        (MODIFY — add duration, calories)
│   │   ├── WorkoutSet.java                     (no change)
│   │   ├── UserProfile.java                    (NEW)
│   │   ├── WeeklyRoutine.java                  (NEW)
│   │   └── RoutineExercise.java                (NEW)
│   └── dao/
│       ├── ExerciseDao.java                    (no change)
│       ├── WorkoutDao.java                     (MODIFY — date-range queries)
│       ├── WorkoutSetDao.java                  (no change)
│       ├── UserProfileDao.java                 (NEW)
│       ├── WeeklyRoutineDao.java               (NEW)
│       └── RoutineExerciseDao.java             (NEW)
├── ui/
│   ├── dashboard/
│   │   ├── DashboardFragment.java              (NEW)
│   │   ├── DashboardViewModel.java             (NEW)
│   │   └── TodayExerciseAdapter.java           (NEW)
│   ├── calendar/
│   │   ├── CalendarFragment.java               (NEW)
│   │   ├── CalendarViewModel.java              (NEW)
│   │   └── CalendarDayAdapter.java             (NEW)
│   ├── routines/
│   │   ├── RoutinesFragment.java               (NEW)
│   │   ├── RoutinesViewModel.java              (NEW)
│   │   ├── RoutineDetailActivity.java          (NEW)
│   │   └── RoutineExerciseAdapter.java         (NEW)
│   ├── profile/
│   │   ├── ProfileFragment.java                (NEW)
│   │   └── ProfileViewModel.java               (NEW)
│   ├── onboarding/
│   │   └── OnboardingActivity.java             (NEW)
│   ├── home/
│   │   ├── HomeViewModel.java                  (DELETE — replaced by DashboardViewModel)
│   │   └── WorkoutAdapter.java                 (MODIFY — add calories display)
│   └── workout/
│       ├── ActiveWorkoutActivity.java          (MODIFY — capture duration, calories)
│       ├── ActiveWorkoutViewModel.java         (MODIFY — calorie calculation)
│       └── ... (other adapters unchanged)
└── util/
    └── CalorieCalculator.java                  (NEW)

app/src/main/res/
├── layout/
│   ├── activity_main.xml                       (MODIFY — nav host)
│   ├── activity_onboarding.xml                 (NEW)
│   ├── activity_routine_detail.xml             (NEW)
│   ├── fragment_dashboard.xml                  (NEW)
│   ├── fragment_calendar.xml                   (NEW)
│   ├── fragment_routines.xml                   (NEW)
│   ├── fragment_profile.xml                    (NEW)
│   ├── item_dashboard_stat.xml                 (NEW)
│   ├── item_today_exercise.xml                 (NEW)
│   ├── item_day_card.xml                       (NEW)
│   ├── item_routine_exercise.xml               (NEW)
│   ├── item_calendar_workout.xml               (NEW)
│   └── item_workout.xml                        (MODIFY — add calories)
├── menu/
│   └── bottom_nav_menu.xml                     (NEW)
├── drawable/
│   ├── ic_dashboard.xml                        (NEW)
│   ├── ic_calendar.xml                         (NEW)
│   ├── ic_routines.xml                         (NEW)
│   └── ic_profile.xml                          (NEW)
└── values/
    ├── colors.xml                              (MODIFY — add gamification colors)
    ├── strings.xml                             (MODIFY — 50+ new strings)
    └── themes.xml                              (no change)
```

**Total: ~30 new files, ~12 modified files**

---

## Open Questions

> [!IMPORTANT]
> **Weight unit preference**: Should the app support both kg and lbs with a toggle in the profile, or stick to one unit? This affects the calorie calculation display and all weight inputs.

> [!IMPORTANT]
> **Onboarding depth**: How detailed should the onboarding be? Options:
> - **Minimal**: Single screen with name, age, gender, height, weight (quick start)
> - **Multi-step wizard**: Animated card-by-card flow with illustrations (more polished but more code)

> [!NOTE]
> **Routine editing scope**: When a user edits a routine (e.g. changes Wednesday from "Push" to "Shoulders"), should it:
> - **A)** Overwrite immediately (simpler — edits always apply from this week onward)
> - **B)** Ask "Apply from next week?" vs "Apply from today?" (more flexible but more complex)
> I recommend option A for V2 simplicity.

> [!NOTE]
> **"Start Today's Workout" behavior**: When tapped, should it auto-populate the active workout with all exercises from today's scheduled routine (with target sets/reps pre-filled), or just use it as a suggestion?

## Verification Plan

### Manual Verification
- Build & run after each phase to verify no compile errors
- Test onboarding flow on first launch
- Test routine scheduling for each day of the week
- Verify calendar shows dots on workout days and detail on tap
- Verify calorie numbers are reasonable (cross-check with online calculators)
- Verify dashboard streak counter, PRs, and stat cards
- Test "Start Today's Workout" pre-populates from schedule
