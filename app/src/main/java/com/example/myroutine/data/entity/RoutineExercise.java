package com.example.myroutine.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Links an {@link Exercise} to a {@link WeeklyRoutine} with target sets/reps.
 * <p>
 * When displayed in the routine editor or pre-populated into an active workout,
 * exercises are ordered by {@code orderIndex}.
 */
@Entity(
    tableName = "routine_exercises",
    foreignKeys = {
        @ForeignKey(
            entity = WeeklyRoutine.class,
            parentColumns = "id",
            childColumns = "routineId",
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
        @Index(value = "routineId"),
        @Index(value = "exerciseId")
    }
)
public class RoutineExercise {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** Foreign key → weekly_routines.id */
    private int routineId;

    /** Foreign key → exercises.id */
    private int exerciseId;

    /** Target number of sets for this exercise in the routine. */
    private int targetSets;

    /** Target number of reps per set. */
    private int targetReps;

    /** Display order within the routine (0-based). */
    private int orderIndex;

    // ── Constructor ─────────────────────────────────────────────────────

    public RoutineExercise(int routineId, int exerciseId, int targetSets, int targetReps, int orderIndex) {
        this.routineId = routineId;
        this.exerciseId = exerciseId;
        this.targetSets = targetSets;
        this.targetReps = targetReps;
        this.orderIndex = orderIndex;
    }

    // ── Getters & Setters ───────────────────────────────────────────────

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getRoutineId() { return routineId; }
    public void setRoutineId(int routineId) { this.routineId = routineId; }

    public int getExerciseId() { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }

    public int getTargetSets() { return targetSets; }
    public void setTargetSets(int targetSets) { this.targetSets = targetSets; }

    public int getTargetReps() { return targetReps; }
    public void setTargetReps(int targetReps) { this.targetReps = targetReps; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
