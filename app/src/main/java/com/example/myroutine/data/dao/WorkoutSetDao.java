package com.example.myroutine.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myroutine.data.entity.WorkoutSet;

import java.util.List;

/**
 * Data Access Object for the {@link WorkoutSet} entity.
 * Provides insert, query, and delete operations for individual sets.
 */
@Dao
public interface WorkoutSetDao {
    @Query("SELECT * FROM workout_sets ORDER BY id ASC")
    LiveData<List<WorkoutSet>> getAll();
    @Query("SELECT * FROM workout_sets ORDER BY id ASC")
    List<WorkoutSet> getAllSync();
    @Query("SELECT ws.* FROM workout_sets ws JOIN workouts w ON w.id = ws.workoutId WHERE exerciseId = :exerciseId ORDER BY w.timestamp DESC, ws.id ASC")
    List<WorkoutSet> getExerciseHistory(int exerciseId);

    /** Insert a single set. */
    @Insert
    void insert(WorkoutSet set);

    /** Insert multiple sets at once (used when finishing a workout). */
    @Insert
    void insertAll(List<WorkoutSet> sets);

    /** Retrieve all sets for a given workout, ordered by ID (insertion order). */
    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId ORDER BY id ASC")
    LiveData<List<WorkoutSet>> getSetsForWorkout(long workoutId);

    /** Retrieve all sets for a given workout synchronously. */
    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId ORDER BY id ASC")
    List<WorkoutSet> getSetsForWorkoutSync(long workoutId);

    /** Delete a single set. */
    @Delete
    void delete(WorkoutSet set);

    /** Delete all sets belonging to a specific workout. */
    @Query("DELETE FROM workout_sets WHERE workoutId = :workoutId")
    void deleteByWorkoutId(long workoutId);
}
