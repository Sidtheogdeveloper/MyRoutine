package com.example.myroutine.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.myroutine.data.dao.ExerciseDao;
import com.example.myroutine.data.dao.RoutineExerciseDao;
import com.example.myroutine.data.dao.UserProfileDao;
import com.example.myroutine.data.dao.WeeklyRoutineDao;
import com.example.myroutine.data.dao.WorkoutDao;
import com.example.myroutine.data.dao.WorkoutSetDao;
import com.example.myroutine.data.entity.Exercise;
import com.example.myroutine.data.entity.RoutineExercise;
import com.example.myroutine.data.entity.UserProfile;
import com.example.myroutine.data.entity.WeeklyRoutine;
import com.example.myroutine.data.entity.Workout;
import com.example.myroutine.data.entity.WorkoutSet;

import java.util.List;

/**
 * Repository that abstracts access to all data sources (V2).
 * <p>
 * ViewModels interact with this class instead of DAOs directly.
 * All write operations are dispatched to the database executor;
 * LiveData queries are handled by Room on its own background threads.
 */
public class WorkoutRepository {

    private final ExerciseDao exerciseDao;
    private final WorkoutDao workoutDao;
    private final WorkoutSetDao workoutSetDao;
    private final UserProfileDao userProfileDao;
    private final WeeklyRoutineDao weeklyRoutineDao;
    private final RoutineExerciseDao routineExerciseDao;

    private final LiveData<List<Exercise>> allExercises;
    private final LiveData<List<Workout>> allWorkouts;

    public WorkoutRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        exerciseDao = db.exerciseDao();
        workoutDao = db.workoutDao();
        workoutSetDao = db.workoutSetDao();
        userProfileDao = db.userProfileDao();
        weeklyRoutineDao = db.weeklyRoutineDao();
        routineExerciseDao = db.routineExerciseDao();

        allExercises = exerciseDao.getAll();
        allWorkouts = workoutDao.getAll();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Exercise operations
    // ═══════════════════════════════════════════════════════════════════

    public LiveData<List<Exercise>> getAllExercises() {
        return allExercises;
    }

    public LiveData<List<Exercise>> searchExercises(String query) {
        return exerciseDao.searchExtended(query);
    }

    /** Get a single exercise synchronously (for background use). */
    public Exercise getExerciseById(int id) {
        return exerciseDao.getById(id);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Workout operations
    // ═══════════════════════════════════════════════════════════════════

    public LiveData<List<Workout>> getAllWorkouts() {
        return allWorkouts;
    }

    public LiveData<List<Workout>> getRecentWorkouts(int limit) {
        return workoutDao.getRecent(limit);
    }

    public void insertWorkout(Workout workout, OnWorkoutInsertedCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = workoutDao.insert(workout);
            if (callback != null) {
                callback.onInserted(id);
            }
        });
    }

    public void deleteWorkout(Workout workout) {
        AppDatabase.databaseWriteExecutor.execute(() -> workoutDao.delete(workout));
    }

    public int getSetCount(long workoutId) {
        return workoutDao.getSetCount(workoutId);
    }

    // ── Date-range queries ──────────────────────────────────────────────

    public LiveData<List<Workout>> getWorkoutsBetween(long startMs, long endMs) {
        return workoutDao.getWorkoutsBetween(startMs, endMs);
    }

    public List<Workout> getWorkoutsBetweenSync(long startMs, long endMs) {
        return workoutDao.getWorkoutsBetweenSync(startMs, endMs);
    }

    // ── Stats ───────────────────────────────────────────────────────────

    public Workout getMostRecentWorkoutSync() {
        return workoutDao.getMostRecentSync();
    }

    public List<Long> getAllTimestampsSync() {
        return workoutDao.getAllTimestampsSync();
    }

    public int getTotalSetsInRange(long startMs, long endMs) {
        return workoutDao.getTotalSetsInRange(startMs, endMs);
    }

    // ═══════════════════════════════════════════════════════════════════
    // WorkoutSet operations
    // ═══════════════════════════════════════════════════════════════════

    public void insertSets(List<WorkoutSet> sets) {
        AppDatabase.databaseWriteExecutor.execute(() -> workoutSetDao.insertAll(sets));
    }

    public LiveData<List<WorkoutSet>> getSetsForWorkout(long workoutId) {
        return workoutSetDao.getSetsForWorkout(workoutId);
    }

    public List<WorkoutSet> getSetsForWorkoutSync(long workoutId) {
        return workoutSetDao.getSetsForWorkoutSync(workoutId);
    }

    // ═══════════════════════════════════════════════════════════════════
    // UserProfile operations
    // ═══════════════════════════════════════════════════════════════════

    public LiveData<UserProfile> getUserProfile() {
        return userProfileDao.getProfile();
    }

    public UserProfile getUserProfileSync() {
        return userProfileDao.getProfileSync();
    }

    public void saveUserProfile(UserProfile profile) {
        AppDatabase.databaseWriteExecutor.execute(() -> userProfileDao.insertOrUpdate(profile));
    }

    /** Check if onboarding is needed (no profile exists). Call from background thread. */
    public boolean isProfileComplete() {
        return userProfileDao.getProfileCount() > 0;
    }

    // ═══════════════════════════════════════════════════════════════════
    // WeeklyRoutine operations
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Get the currently active routine for a day of the week.
     * Synchronous — call from a background thread.
     *
     * @param dayOfWeek 1=Monday ... 7=Sunday
     */
    public WeeklyRoutine getActiveRoutineForDay(int dayOfWeek) {
        return weeklyRoutineDao.getActiveRoutineForDay(dayOfWeek, System.currentTimeMillis());
    }

    public LiveData<WeeklyRoutine> getActiveRoutineForDayLive(int dayOfWeek) {
        return weeklyRoutineDao.getActiveRoutineForDayLive(dayOfWeek, System.currentTimeMillis());
    }

    public LiveData<List<WeeklyRoutine>> getAllActiveRoutines() {
        return weeklyRoutineDao.getAllActiveRoutines(System.currentTimeMillis());
    }

    public void insertRoutine(WeeklyRoutine routine, OnRoutineInsertedCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = weeklyRoutineDao.insert(routine);
            if (callback != null) {
                callback.onInserted((int) id);
            }
        });
    }

    public void deleteRoutine(WeeklyRoutine routine) {
        AppDatabase.databaseWriteExecutor.execute(() -> weeklyRoutineDao.delete(routine));
    }

    public WeeklyRoutine getRoutineById(int id) {
        return weeklyRoutineDao.getById(id);
    }

    // ═══════════════════════════════════════════════════════════════════
    // RoutineExercise operations
    // ═══════════════════════════════════════════════════════════════════

    public LiveData<List<RoutineExercise>> getExercisesForRoutine(int routineId) {
        return routineExerciseDao.getExercisesForRoutine(routineId);
    }

    public List<RoutineExercise> getExercisesForRoutineSync(int routineId) {
        return routineExerciseDao.getExercisesForRoutineSync(routineId);
    }

    public void insertRoutineExercise(RoutineExercise re) {
        AppDatabase.databaseWriteExecutor.execute(() -> routineExerciseDao.insert(re));
    }

    public void insertRoutineExercises(List<RoutineExercise> list) {
        AppDatabase.databaseWriteExecutor.execute(() -> routineExerciseDao.insertAll(list));
    }

    public void deleteRoutineExercise(RoutineExercise re) {
        AppDatabase.databaseWriteExecutor.execute(() -> routineExerciseDao.delete(re));
    }

    public void clearRoutineExercises(int routineId) {
        AppDatabase.databaseWriteExecutor.execute(() -> routineExerciseDao.deleteByRoutineId(routineId));
    }

    // ═══════════════════════════════════════════════════════════════════
    // Callback interfaces
    // ═══════════════════════════════════════════════════════════════════

    public interface OnWorkoutInsertedCallback {
        void onInserted(long workoutId);
    }

    public interface OnRoutineInsertedCallback {
        void onInserted(int routineId);
    }
}
