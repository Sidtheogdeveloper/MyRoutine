package com.example.myroutine.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myroutine.data.entity.RoutineExercise;

import java.util.List;

/**
 * Data Access Object for the {@link RoutineExercise} entity.
 * Links exercises to weekly routines with target sets/reps.
 */
@Dao
public interface RoutineExerciseDao {

    /** Insert a single routine exercise entry. */
    @Insert
    void insert(RoutineExercise routineExercise);

    /** Insert multiple entries at once. */
    @Insert
    void insertAll(List<RoutineExercise> routineExercises);

    /** Delete a single entry. */
    @Delete
    void delete(RoutineExercise routineExercise);

    /** Delete all exercises for a given routine. */
    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    void deleteByRoutineId(int routineId);

    /** Observe all exercises for a routine, ordered by orderIndex. */
    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    LiveData<List<RoutineExercise>> getExercisesForRoutine(int routineId);

    /** Get all exercises for a routine synchronously (for pre-populating active workouts). */
    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    List<RoutineExercise> getExercisesForRoutineSync(int routineId);
}
