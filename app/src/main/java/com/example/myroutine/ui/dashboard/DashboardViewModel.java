package com.example.myroutine.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myroutine.data.AppDatabase;
import com.example.myroutine.data.WorkoutRepository;
import com.example.myroutine.data.entity.Exercise;
import com.example.myroutine.data.entity.RoutineExercise;
import com.example.myroutine.data.entity.UserProfile;
import com.example.myroutine.data.entity.WeeklyRoutine;
import com.example.myroutine.data.entity.Workout;
import com.example.myroutine.data.entity.WorkoutSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ViewModel for the gamified Dashboard.
 * <p>
 * Aggregates: user greeting, streak, today's routine with exercises,
 * stat cards (last session calories, PR, weekly sets), and recent workouts.
 */
public class DashboardViewModel extends AndroidViewModel {

    private final WorkoutRepository repository;

    private final LiveData<UserProfile> userProfile;
    private final LiveData<List<Workout>> recentWorkouts;

    // Computed stats (loaded asynchronously)
    private final MutableLiveData<Integer> streak = new MutableLiveData<>(0);
    private final MutableLiveData<Double> lastSessionCalories = new MutableLiveData<>(0.0);
    private final MutableLiveData<Integer> setsThisWeek = new MutableLiveData<>(0);
    private final MutableLiveData<String> prValue = new MutableLiveData<>("--");
    private final MutableLiveData<String> prLabel = new MutableLiveData<>("PR");

    // Today's routine
    private final MutableLiveData<WeeklyRoutine> todayRoutine = new MutableLiveData<>();
    private final MutableLiveData<List<TodayExerciseItem>> todayExercises = new MutableLiveData<>(new ArrayList<>());

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutRepository(application);
        userProfile = repository.getUserProfile();
        recentWorkouts = repository.getRecentWorkouts(5);

        loadStats();
        loadTodayRoutine();
    }

    // ── Getters ─────────────────────────────────────────────────────────

    public LiveData<UserProfile> getUserProfile() { return userProfile; }
    public LiveData<List<Workout>> getRecentWorkouts() { return recentWorkouts; }
    public LiveData<Integer> getStreak() { return streak; }
    public LiveData<Double> getLastSessionCalories() { return lastSessionCalories; }
    public LiveData<Integer> getSetsThisWeek() { return setsThisWeek; }
    public LiveData<String> getPrValue() { return prValue; }
    public LiveData<String> getPrLabel() { return prLabel; }
    public LiveData<WeeklyRoutine> getTodayRoutine() { return todayRoutine; }
    public LiveData<List<TodayExerciseItem>> getTodayExercises() { return todayExercises; }

    // ── Delete workout ──────────────────────────────────────────────────

    public void deleteWorkout(Workout workout) {
        repository.deleteWorkout(workout);
        // Refresh stats after deletion
        loadStats();
    }

    public int getSetCount(long workoutId) {
        return repository.getSetCount(workoutId);
    }

    // ── Stats computation ───────────────────────────────────────────────

    public void loadStats() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Last session calories
            Workout recent = repository.getMostRecentWorkoutSync();
            if (recent != null) {
                lastSessionCalories.postValue(recent.getCaloriesBurnt());
            }

            // Streak
            int computed = computeStreak();
            streak.postValue(computed);

            // Sets this week
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long weekStart = cal.getTimeInMillis();
            long now = System.currentTimeMillis();
            int sets = repository.getTotalSetsInRange(weekStart, now);
            setsThisWeek.postValue(sets);

            // PR for today's muscle group
            loadPR();
        });
    }

    /**
     * Computes the current workout streak: consecutive days with at least one workout,
     * counting backwards from today.
     */
    private int computeStreak() {
        List<Long> timestamps = repository.getAllTimestampsSync();
        if (timestamps == null || timestamps.isEmpty()) return 0;

        // Build set of workout day offsets (days since epoch)
        Set<Long> workoutDays = new HashSet<>();
        for (long ts : timestamps) {
            workoutDays.add(ts / 86400000L); // Convert to day number
        }

        long today = System.currentTimeMillis() / 86400000L;
        int count = 0;

        // Start from today, go backwards
        for (long day = today; day >= today - 365; day--) {
            if (workoutDays.contains(day)) {
                count++;
            } else {
                // If today has no workout yet, still allow yesterday to start the streak
                if (day == today && count == 0) continue;
                break;
            }
        }

        return count;
    }

    private void loadPR() {
        // Find today's routine and get its exercises' muscle group
        int todayDow = getTodayDayOfWeek();
        WeeklyRoutine routine = repository.getActiveRoutineForDay(todayDow);
        if (routine != null && !routine.isRestDay()) {
            // Get exercises for this routine and find max weight for any of them
            List<RoutineExercise> routineExercises = repository.getExercisesForRoutineSync(routine.getId());
            double maxWeight = 0;
            String maxExercise = "";

            for (RoutineExercise re : routineExercises) {
                Exercise ex = repository.getExerciseById(re.getExerciseId());
                if (ex != null) {
                    // Get all workout sets for this exercise
                    List<Workout> allWorkouts = repository.getWorkoutsBetweenSync(0, System.currentTimeMillis());
                    for (Workout w : allWorkouts) {
                        List<WorkoutSet> sets = repository.getSetsForWorkoutSync(w.getId());
                        for (WorkoutSet s : sets) {
                            if (s.getExerciseId() == ex.getId() && s.getWeight() > maxWeight) {
                                maxWeight = s.getWeight();
                                maxExercise = ex.getName();
                            }
                        }
                    }
                }
            }

            if (maxWeight > 0) {
                prValue.postValue(String.valueOf((int) maxWeight) + "kg");
                prLabel.postValue("PR\n" + maxExercise);
            }
        }
    }

    // ── Today's routine ─────────────────────────────────────────────────

    private void loadTodayRoutine() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int todayDow = getTodayDayOfWeek();
            WeeklyRoutine routine = repository.getActiveRoutineForDay(todayDow);
            todayRoutine.postValue(routine);

            if (routine != null && !routine.isRestDay()) {
                List<RoutineExercise> reList = repository.getExercisesForRoutineSync(routine.getId());
                List<TodayExerciseItem> items = new ArrayList<>();
                for (RoutineExercise re : reList) {
                    Exercise ex = repository.getExerciseById(re.getExerciseId());
                    if (ex != null) {
                        items.add(new TodayExerciseItem(ex.getName(), re.getTargetSets(), re.getTargetReps()));
                    }
                }
                todayExercises.postValue(items);
            }
        });
    }

    /**
     * Returns the current day of week as 1=Monday ... 7=Sunday.
     */
    private int getTodayDayOfWeek() {
        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        // Convert from Calendar (Sun=1..Sat=7) to our format (Mon=1..Sun=7)
        return dow == Calendar.SUNDAY ? 7 : dow - 1;
    }

    // ── Inner data class ────────────────────────────────────────────────

    public static class TodayExerciseItem {
        public final String name;
        public final int targetSets;
        public final int targetReps;

        public TodayExerciseItem(String name, int targetSets, int targetReps) {
            this.name = name;
            this.targetSets = targetSets;
            this.targetReps = targetReps;
        }
    }
}
