package com.example.myroutine.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Stores the user's physical profile for calorie calculations
 * and personalized dashboard greetings.
 * <p>
 * Singleton pattern: only one row (id=1) ever exists in this table.
 */
@Entity(tableName = "user_profile")
public class UserProfile {

    @PrimaryKey
    private int id;

    /** Display name (used in dashboard greetings). */
    private String name;

    /** User's age in years. */
    private int age;

    /** "Male", "Female", or "Other". */
    private String gender;

    /** Height in centimeters. */
    private double heightCm;

    /** Weight in kilograms. */
    private double weightKg;

    /** Body fat percentage. -1 means not provided (optional field). */
    private double bodyFatPercentage;

    /** Timestamp when the profile was first created. */
    private long createdAt;

    /** Timestamp of the last profile update. */
    private long updatedAt;

    // ── Constructor ─────────────────────────────────────────────────────

    public UserProfile(String name, int age, String gender,
                       double heightCm, double weightKg, double bodyFatPercentage) {
        this.id = 1; // Singleton — always row 1
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.bodyFatPercentage = bodyFatPercentage;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // ── Getters & Setters ───────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public double getBodyFatPercentage() { return bodyFatPercentage; }
    public void setBodyFatPercentage(double bodyFatPercentage) { this.bodyFatPercentage = bodyFatPercentage; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    /** Returns true if body fat percentage was provided. */
    public boolean hasBodyFat() {
        return bodyFatPercentage >= 0;
    }
}
