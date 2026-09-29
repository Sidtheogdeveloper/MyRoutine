package com.example.myroutine.util;

import android.content.Context;
import android.util.Log;

import com.example.myroutine.data.entity.Exercise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses exercises from the packaged assets/exercises.csv file
 * into {@link Exercise} entity objects, calculating appropriate MET values
 * for calorie estimation.
 */
public class ExerciseCsvParser {

    private static final String TAG = "ExerciseCsvParser";
    public static final String DEFAULT_ASSET_FILE = "exercises.csv";

    /**
     * Parses the CSV file from assets and returns the list of Exercise entities.
     *
     * @param context Application context to access assets.
     * @return List of parsed exercises (or fallback defaults on read error).
     */
    public static List<Exercise> parseFromAssets(Context context) {
        return parseFromAssets(context, DEFAULT_ASSET_FILE);
    }

    public static List<Exercise> parseFromAssets(Context context, String assetFileName) {
        List<Exercise> exercises = new ArrayList<>();

        try (InputStream is = context.getAssets().open(assetFileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // CSV format: exercise,primary_muscle,secondary_muscles,movement_pattern,equipment,unilateral,compound_or_isolation,difficulty
                String[] tokens = line.split(",", -1);
                if (tokens.length < 8) {
                    // Line might have commas inside or fewer tokens
                    Log.w(TAG, "Skipping malformed CSV line: " + line);
                    continue;
                }

                String name = tokens[0].trim();
                String primaryMuscle = tokens[1].trim();
                String secondaryMuscles = tokens[2].trim();
                String movementPattern = tokens[3].trim();
                String equipment = tokens[4].trim();
                boolean unilateral = Boolean.parseBoolean(tokens[5].trim());
                String compoundOrIsolation = tokens[6].trim();
                String difficulty = tokens[7].trim();

                double metValue = calculateMetValue(compoundOrIsolation, movementPattern, primaryMuscle, difficulty);

                Exercise exercise = new Exercise(
                        name,
                        primaryMuscle,
                        secondaryMuscles,
                        movementPattern,
                        equipment,
                        unilateral,
                        compoundOrIsolation,
                        difficulty,
                        metValue
                );

                exercises.add(exercise);
            }

            Log.i(TAG, "Successfully parsed " + exercises.size() + " exercises from " + assetFileName);

        } catch (IOException e) {
            Log.e(TAG, "Failed to read exercises from assets: " + assetFileName, e);
        }

        if (exercises.isEmpty()) {
            exercises = getFallbackExercises();
        }

        return exercises;
    }

    /**
     * Calculates the Metabolic Equivalent of Task (MET) value based on
     * movement type, muscle mass recruited, movement pattern, and difficulty.
     */
    public static double calculateMetValue(String compoundOrIsolation, String movementPattern,
                                           String primaryMuscle, String difficulty) {
        String type = compoundOrIsolation != null ? compoundOrIsolation.trim().toLowerCase() : "";
        String pattern = movementPattern != null ? movementPattern.trim().toLowerCase() : "";
        String muscle = primaryMuscle != null ? primaryMuscle.trim().toLowerCase() : "";
        String diff = difficulty != null ? difficulty.trim().toLowerCase() : "";

        // Olympic / high power movements (snatch, clean, jerks, full body)
        if (pattern.contains("olympic") || pattern.contains("clean") || pattern.contains("snatch")
                || muscle.contains("full body") || pattern.contains("crawl") || pattern.contains("carry")) {
            return 7.5;
        }

        // Heavy compound lower body & posterior chain
        if (pattern.contains("squat") || pattern.contains("deadlift") || pattern.contains("hinge")
                || muscle.contains("quads") || muscle.contains("glutes") || muscle.contains("hamstrings")) {
            if (type.equals("compound")) {
                return diff.equals("advanced") ? 6.5 : 6.0;
            }
        }

        // Upper body compound
        if (type.equals("compound")) {
            if (diff.equals("advanced")) return 6.0;
            if (diff.equals("intermediate")) return 5.5;
            return 5.0;
        }

        // Isometric movements (planks, holds)
        if (type.equals("isometric")) {
            return 3.0;
        }

        // Isolation: Small muscle groups vs larger isolation
        if (muscle.contains("abs") || muscle.contains("core") || muscle.contains("calves")
                || muscle.contains("forearms") || muscle.contains("grip") || muscle.contains("neck")) {
            return 3.0;
        }

        return 3.5;
    }

    /**
     * Minimal fallback set of exercises if assets cannot be read.
     */
    private static List<Exercise> getFallbackExercises() {
        List<Exercise> list = new ArrayList<>();
        list.add(new Exercise("Barbell Bench Press", "Chest", "Front Delts;Triceps", "Horizontal Push", "Barbell", false, "Compound", "Intermediate", 6.0));
        list.add(new Exercise("Barbell Back Squat", "Quads", "Glutes;Hamstrings;Core", "Squat", "Barbell", false, "Compound", "Intermediate", 6.0));
        list.add(new Exercise("Conventional Deadlift", "Erector Spinae", "Glutes;Hamstrings;Lats", "Hip Hinge", "Barbell", false, "Compound", "Advanced", 6.5));
        list.add(new Exercise("Pull-Up", "Lats", "Biceps;Traps", "Vertical Pull", "Pull-Up Bar", false, "Compound", "Intermediate", 5.5));
        list.add(new Exercise("Overhead Barbell Press", "Shoulders", "Triceps;Upper Chest", "Vertical Push", "Barbell", false, "Compound", "Intermediate", 5.0));
        return list;
    }
}
