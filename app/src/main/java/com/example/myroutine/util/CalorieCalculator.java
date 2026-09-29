package com.example.myroutine.util;

import com.example.myroutine.data.entity.Exercise;
import com.example.myroutine.ui.workout.ActiveWorkoutViewModel.ExerciseWithSets;
import com.example.myroutine.ui.workout.ActiveWorkoutViewModel.SetData;

import java.util.List;

/**
 * Utility class for estimating calories burnt during a workout
 * using the MET (Metabolic Equivalent of Task) formula.
 * <p>
 * Formula: Calories = MET × weightKg × durationHours
 * <p>
 * For strength training, we weight each exercise's contribution
 * proportionally to its time share of the total workout, based
 * on the number of sets performed per exercise.
 */
public final class CalorieCalculator {

    private CalorieCalculator() {} // Prevent instantiation

    /**
     * Calculate estimated calories burnt for a full workout session.
     * <p>
     * Approach: For each exercise, compute its MET contribution weighted
     * by its share of total sets. This gives a weighted-average MET
     * across the session, which is then multiplied by user weight and duration.
     *
     * @param exercises       The exercises performed with their sets
     * @param userWeightKg    User's weight in kilograms
     * @param durationSeconds Total workout duration in seconds
     * @return Estimated calories burnt (rounded to 1 decimal)
     */
    public static double calculateCalories(List<ExerciseWithSets> exercises,
                                           double userWeightKg,
                                           long durationSeconds) {
        if (exercises == null || exercises.isEmpty() || durationSeconds <= 0 || userWeightKg <= 0) {
            return 0.0;
        }

        // Count total non-empty sets and compute weighted MET sum
        int totalSets = 0;
        double weightedMetSum = 0.0;

        for (ExerciseWithSets ews : exercises) {
            int setsForExercise = 0;
            for (SetData sd : ews.sets) {
                if (sd.complete && sd.reps > 0) {
                    setsForExercise++;
                }
            }
            if (setsForExercise > 0) {
                totalSets += setsForExercise;
                weightedMetSum += ews.exercise.getMetValue() * setsForExercise;
            }
        }

        if (totalSets == 0) return 0.0;

        // Weighted average MET
        double averageMet = weightedMetSum / totalSets;

        // Duration in hours
        double durationHours = durationSeconds / 3600.0;

        // Calories = MET × weight(kg) × duration(hours)
        double calories = averageMet * userWeightKg * durationHours;

        // Round to 1 decimal place
        return Math.round(calories * 10.0) / 10.0;
    }

    /**
     * Quick estimate using a single MET value (for simpler contexts).
     *
     * @param metValue        MET value for the activity
     * @param userWeightKg    User's weight in kg
     * @param durationSeconds Duration in seconds
     * @return Estimated calories
     */
    public static double calculateSimple(double metValue, double userWeightKg, long durationSeconds) {
        if (metValue <= 0 || userWeightKg <= 0 || durationSeconds <= 0) return 0.0;
        double durationHours = durationSeconds / 3600.0;
        return Math.round(metValue * userWeightKg * durationHours * 10.0) / 10.0;
    }
}
