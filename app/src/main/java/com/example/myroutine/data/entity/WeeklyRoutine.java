package com.example.myroutine.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a named routine assigned to a specific day of the week.
 * <p>
 * Each row maps one day (Monday=1 ... Sunday=7) to a routine name like
 * "Push Day" or "Legs". The {@code effectiveFrom} timestamp enables
 * versioning: edits create new rows so only future weeks are affected.
 * <p>
 * A day marked as a rest day has {@code isRestDay = true} and no
 * associated {@link RoutineExercise} entries.
 */
@Entity(tableName = "weekly_routines")
public class WeeklyRoutine {

    @PrimaryKey(autoGenerate = true)
    private int id;

    /**
     * Day of the week: 1=Monday, 2=Tuesday, ..., 7=Sunday.
     * Matches {@link java.util.Calendar#DAY_OF_WEEK} after remapping.
     */
    private int dayOfWeek;

    /** Human-readable routine name (e.g. "Push Day", "Legs & Core"). */
    private String routineName;

    /** If true, this day is a rest day with no exercises. */
    private boolean isRestDay;

    /**
     * Unix timestamp (ms) from which this routine version is active.
     * Allows "changes apply to future" by inserting a new row with
     * effectiveFrom = now and querying with ORDER BY effectiveFrom DESC LIMIT 1.
     */
    private long effectiveFrom;

    // ── Constructor ─────────────────────────────────────────────────────

    public WeeklyRoutine(int dayOfWeek, String routineName, boolean isRestDay, long effectiveFrom) {
        this.dayOfWeek = dayOfWeek;
        this.routineName = routineName;
        this.isRestDay = isRestDay;
        this.effectiveFrom = effectiveFrom;
    }

    // ── Getters & Setters ───────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getRoutineName() { return routineName; }
    public void setRoutineName(String routineName) { this.routineName = routineName; }

    public boolean isRestDay() { return isRestDay; }
    public void setRestDay(boolean restDay) { isRestDay = restDay; }

    public long getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(long effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    // ── Utility ─────────────────────────────────────────────────────────

    /** Returns the day name for display (e.g. "Monday", "Tuesday"). */
    public static String dayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1: return "Monday";
            case 2: return "Tuesday";
            case 3: return "Wednesday";
            case 4: return "Thursday";
            case 5: return "Friday";
            case 6: return "Saturday";
            case 7: return "Sunday";
            default: return "Unknown";
        }
    }
}
