package com.example.myroutine.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myroutine.data.entity.Workout;

import java.util.List;

/**
 * Data Access Object for the {@link Workout} entity.
 * Provides insert, query, and delete operations for workout sessions.
 */
@Dao
public interface WorkoutDao {

    /**
     * Insert a new workout and return its auto-generated row ID.
     * The returned ID is used to associate {@link com.example.myroutine.data.entity.WorkoutSet}
     * rows with this workout.
     */
    @Insert
    long insert(Workout workout);

    /** Retrieve all workouts ordered by most recent first. */
    @Query("SELECT * FROM workouts ORDER BY timestamp DESC")
    LiveData<List<Workout>> getAll();

    /** Get recent workouts (limited count) for the dashboard. */
    @Query("SELECT * FROM workouts ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<Workout>> getRecent(int limit);

    /** Find a single workout by its ID. */
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    Workout getById(long workoutId);

    /** Delete a workout. Cascade delete will also remove all associated sets. */
    @Delete
    void delete(Workout workout);

    /** Get the total number of sets associated with a workout (for display in the list). */
    @Query("SELECT COUNT(*) FROM workout_sets WHERE workoutId = :workoutId")
    int getSetCount(long workoutId);

    // ── Date-range queries (for Calendar) ───────────────────────────────

    /**
     * Get all workouts within a date range (inclusive).
     * Used by the Calendar view to show workout dots and by the dashboard for stats.
     *
     * @param startMs Start of range (Unix timestamp ms, inclusive)
     * @param endMs   End of range (Unix timestamp ms, inclusive)
     */
    @Query("SELECT * FROM workouts WHERE timestamp >= :startMs AND timestamp <= :endMs ORDER BY timestamp DESC")
    LiveData<List<Workout>> getWorkoutsBetween(long startMs, long endMs);

    /** Synchronous variant of date-range query (for background stats computation). */
    @Query("SELECT * FROM workouts WHERE timestamp >= :startMs AND timestamp <= :endMs ORDER BY timestamp DESC")
    List<Workout> getWorkoutsBetweenSync(long startMs, long endMs);

    // ── Stats queries ───────────────────────────────────────────────────

    /** Get the most recent workout (for "last session" stats). */
    @Query("SELECT * FROM workouts ORDER BY timestamp DESC LIMIT 1")
    Workout getMostRecentSync();

    /** Count distinct days with workouts in a date range (for streak calculation). */
    @Query("SELECT COUNT(DISTINCT(timestamp / 86400000)) FROM workouts WHERE timestamp >= :startMs AND timestamp <= :endMs")
    int getDistinctWorkoutDays(long startMs, long endMs);

    /** Get all workout timestamps (for streak calculation). Ordered most recent first. */
    @Query("SELECT timestamp FROM workouts ORDER BY timestamp DESC")
    List<Long> getAllTimestampsSync();

    /** Get total sets completed in a date range. */
    @Query("SELECT COUNT(*) FROM workout_sets ws INNER JOIN workouts w ON ws.workoutId = w.id " +
           "WHERE w.timestamp >= :startMs AND w.timestamp <= :endMs")
    int getTotalSetsInRange(long startMs, long endMs);
}
