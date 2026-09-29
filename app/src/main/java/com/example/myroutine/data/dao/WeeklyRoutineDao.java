package com.example.myroutine.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myroutine.data.entity.WeeklyRoutine;

import java.util.List;

/**
 * Data Access Object for the {@link WeeklyRoutine} entity.
 * Supports versioned routines via the effectiveFrom timestamp.
 */
@Dao
public interface WeeklyRoutineDao {
    @Query("SELECT * FROM weekly_routines ORDER BY dayOfWeek ASC, effectiveFrom DESC, id DESC")
    LiveData<List<WeeklyRoutine>> getAllRoutinesLive();

    /** Insert a new routine version. Returns the auto-generated ID. */
    @Insert
    long insert(WeeklyRoutine routine);

    /** Update an existing routine. */
    @Update
    void update(WeeklyRoutine routine);

    /** Delete a routine (cascades to its RoutineExercise entries). */
    @Delete
    void delete(WeeklyRoutine routine);

    /**
     * Get the currently active routine for a specific day of the week.
     * Returns the most recently effective routine (effectiveFrom <= now).
     *
     * @param dayOfWeek 1=Monday ... 7=Sunday
     * @param now       Current timestamp in milliseconds
     */
    @Query("SELECT * FROM weekly_routines WHERE dayOfWeek = :dayOfWeek AND effectiveFrom <= :now " +
           "ORDER BY effectiveFrom DESC LIMIT 1")
    WeeklyRoutine getActiveRoutineForDay(int dayOfWeek, long now);

    /**
     * Observe the active routine for a specific day (LiveData variant).
     */
    @Query("SELECT * FROM weekly_routines WHERE dayOfWeek = :dayOfWeek AND effectiveFrom <= :now " +
           "ORDER BY effectiveFrom DESC LIMIT 1")
    LiveData<WeeklyRoutine> getActiveRoutineForDayLive(int dayOfWeek, long now);

    /**
     * Get the active routines for ALL days of the week (for the weekly grid view).
     * Returns one row per day — the most recently effective version.
     * <p>
     * Note: This returns ALL versions. The caller should group by dayOfWeek
     * and pick the most recent effectiveFrom for each day.
     */
    @Query("SELECT * FROM weekly_routines WHERE effectiveFrom <= :now ORDER BY dayOfWeek ASC, effectiveFrom DESC")
    LiveData<List<WeeklyRoutine>> getAllActiveRoutines(long now);

    /** Get all routines ever created (for debugging / management). */
    @Query("SELECT * FROM weekly_routines ORDER BY dayOfWeek ASC, effectiveFrom DESC")
    List<WeeklyRoutine> getAllSync();

    /** Get a routine by ID. */
    @Query("SELECT * FROM weekly_routines WHERE id = :id")
    WeeklyRoutine getById(int id);
}
