package com.example.myroutine.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Represents a single exercise in the exercise library.
 * Pre-populated from the comprehensive workout database CSV.
 * Each exercise contains primary/secondary muscles, movement pattern,
 * equipment, unilateral flag, compound/isolation type, difficulty,
 * and a MET value used for calorie estimation.
 */
@Entity(tableName = "exercises")
public class Exercise {

    @PrimaryKey(autoGenerate = true)
    private int id;

    /** Display name of the exercise (e.g. "Barbell Bench Press"). */
    private String name;

    /** Primary target muscle group (e.g. "Chest", "Quads", "Lats"). */
    private String muscleGroup;

    /** Secondary muscles involved (e.g. "Front Delts;Triceps"). */
    private String secondaryMuscles;

    /** Movement pattern (e.g. "Horizontal Push", "Vertical Pull", "Squat"). */
    private String movementPattern;

    /** Equipment required (e.g. "Barbell", "Dumbbell", "Cable", "Machine"). */
    private String equipment;

    /** Whether the movement is unilateral (single-arm / single-leg). */
    private boolean unilateral;

    /** Movement type: "Compound", "Isolation", or "Isometric". */
    private String compoundOrIsolation;

    /** Exercise difficulty: "Beginner", "Intermediate", "Advanced". */
    private String difficulty;

    /**
     * Metabolic Equivalent of Task — used for calorie calculation.
     * Calculated from movement complexity, muscle recruitment, and load.
     */
    private double metValue;

    // ── Constructors ────────────────────────────────────────────────────

    public Exercise(String name, String muscleGroup, String secondaryMuscles,
                    String movementPattern, String equipment, boolean unilateral,
                    String compoundOrIsolation, String difficulty, double metValue) {
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.secondaryMuscles = secondaryMuscles;
        this.movementPattern = movementPattern;
        this.equipment = equipment;
        this.unilateral = unilateral;
        this.compoundOrIsolation = compoundOrIsolation;
        this.difficulty = difficulty;
        this.metValue = metValue;
    }

    @Ignore
    public Exercise(String name, String muscleGroup, double metValue) {
        this(name, muscleGroup, "", "", "", false, "Compound", "Beginner", metValue);
    }

    @Ignore
    public Exercise() {
    }

    // ── Getters & Setters ───────────────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public String getSecondaryMuscles() {
        return secondaryMuscles;
    }

    public void setSecondaryMuscles(String secondaryMuscles) {
        this.secondaryMuscles = secondaryMuscles;
    }

    public String getMovementPattern() {
        return movementPattern;
    }

    public void setMovementPattern(String movementPattern) {
        this.movementPattern = movementPattern;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public boolean isUnilateral() {
        return unilateral;
    }

    public void setUnilateral(boolean unilateral) {
        this.unilateral = unilateral;
    }

    public String getCompoundOrIsolation() {
        return compoundOrIsolation;
    }

    public void setCompoundOrIsolation(String compoundOrIsolation) {
        this.compoundOrIsolation = compoundOrIsolation;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public double getMetValue() {
        return metValue;
    }

    public void setMetValue(double metValue) {
        this.metValue = metValue;
    }
}
