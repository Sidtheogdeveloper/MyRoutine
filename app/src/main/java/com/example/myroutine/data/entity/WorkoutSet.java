package com.example.myroutine.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Represents a single set performed during a workout for a specific exercise.
 * Links to both the parent {@link Workout} and the {@link Exercise} via foreign keys.
 *
 * Cascade-delete ensures that when a Workout is deleted, all its sets are
 * automatically removed.
 */
@Entity(
    tableName = "workout_sets",
    foreignKeys = {
        @ForeignKey(
            entity = Workout.class,
            parentColumns = "id",
            childColumns = "workoutId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Exercise.class,
            parentColumns = "id",
            childColumns = "exerciseId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = "workoutId"),
        @Index(value = "exerciseId")
    }
)
public class WorkoutSet {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** Foreign key → workouts.id */
    private long workoutId;

    /** Foreign key → exercises.id */
    private int exerciseId;

    /** Number of repetitions performed in this set. */
    private int reps;

    /** Weight used in this set (in the user's preferred unit). */
    private double weight;
    @androidx.room.ColumnInfo(defaultValue = "'Working'")
    private String setType = "Working";
    @androidx.room.ColumnInfo(defaultValue = "0")
    private double rpe;
    public String getSetType() { return setType; }
    public void setSetType(String value) { setType = value; }
    public double getRpe() { return rpe; }
    public void setRpe(double value) { rpe = value; }

    // ── Constructors ────────────────────────────────────────────────────

    public WorkoutSet(long workoutId, int exerciseId, int reps, double weight) {
        this.workoutId = workoutId;
        this.exerciseId = exerciseId;
        this.reps = reps;
        this.weight = weight;
    }

    // ── Getters & Setters ───────────────────────────────────────────────

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(long workoutId) {
        this.workoutId = workoutId;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
