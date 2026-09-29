package com.example.myroutine.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a single workout session.
 * Stores the session name, timestamp, duration, and estimated calories burnt.
 */
@Entity(tableName = "workouts")
public class Workout {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** User-editable name (auto-generated as "Workout — <date>" on creation). */
    private String name;

    /** Unix timestamp in milliseconds when the workout was started. */
    private long timestamp;

    /** Actual workout duration in seconds (captured from the chronometer). */
    private long durationSeconds;

    /** Estimated calories burnt, calculated using MET values and user weight. */
    private double caloriesBurnt;
    @androidx.room.ColumnInfo(defaultValue = "''")
    private String notes = "";
    public String getNotes() { return notes; }
    public void setNotes(String value) { notes = value; }

    // ── Constructors ────────────────────────────────────────────────────

    public Workout(String name, long timestamp) {
        this.name = name;
        this.timestamp = timestamp;
        this.durationSeconds = 0;
        this.caloriesBurnt = 0.0;
    }

    // ── Getters & Setters ───────────────────────────────────────────────

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public double getCaloriesBurnt() {
        return caloriesBurnt;
    }

    public void setCaloriesBurnt(double caloriesBurnt) {
        this.caloriesBurnt = caloriesBurnt;
    }
}
