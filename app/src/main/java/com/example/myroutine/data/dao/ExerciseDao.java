package com.example.myroutine.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myroutine.data.entity.Exercise;

import java.util.List;

/**
 * Data Access Object for the {@link Exercise} entity.
 * Provides queries for the exercise library (browse, search, filter).
 */
@Dao
public interface ExerciseDao {

    /** Insert a single exercise. Used during database pre-population. */
    @Insert
    void insert(Exercise exercise);

    /** Insert multiple exercises at once. Used during database pre-population. */
    @Insert
    void insertAll(List<Exercise> exercises);

    /** Returns the total count of exercises in the database. */
    @Query("SELECT COUNT(*) FROM exercises")
    int getCount();

    /** Retrieve all exercises, ordered alphabetically by name. */
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    LiveData<List<Exercise>> getAll();

    /** Retrieve all exercises synchronously (for non-LiveData use cases). */
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    List<Exercise> getAllSync();

    /** Find a single exercise by its ID. */
    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    Exercise getById(int exerciseId);

    /** Filter exercises by muscle group. */
    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup ORDER BY name ASC")
    LiveData<List<Exercise>> getByMuscleGroup(String muscleGroup);

    /** Filter exercises by equipment. */
    @Query("SELECT * FROM exercises WHERE equipment = :equipment ORDER BY name ASC")
    LiveData<List<Exercise>> getByEquipment(String equipment);

    /** Search exercises whose name contains the query string (case-insensitive). */
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<Exercise>> search(String query);

    /** Search across name, muscle group, equipment, and secondary muscles. */
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' " +
           "OR muscleGroup LIKE '%' || :query || '%' " +
           "OR equipment LIKE '%' || :query || '%' " +
           "OR secondaryMuscles LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<Exercise>> searchExtended(String query);
}
