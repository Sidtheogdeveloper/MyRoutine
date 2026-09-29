package com.example.myroutine.ui.workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myroutine.R;
import com.example.myroutine.data.entity.Exercise;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the exercise picker dialog's RecyclerView.
 * Displays exercise name, equipment/details, and muscle group; click selects the exercise.
 */
public class ExercisePickAdapter extends RecyclerView.Adapter<ExercisePickAdapter.PickViewHolder> {

    private List<Exercise> exercises = new ArrayList<>();
    private final OnExercisePickedListener listener;

    /**
     * Callback when the user taps an exercise in the picker.
     */
    public interface OnExercisePickedListener {
        void onExercisePicked(Exercise exercise);
    }

    public ExercisePickAdapter(OnExercisePickedListener listener) {
        this.listener = listener;
    }

    /**
     * Replace the dataset (used for search filtering).
     */
    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PickViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_pick, parent, false);
        return new PickViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PickViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.textName.setText(exercise.getName());
        holder.textMuscleGroup.setText(exercise.getMuscleGroup());

        StringBuilder details = new StringBuilder();
        if (exercise.getEquipment() != null && !exercise.getEquipment().isEmpty()) {
            details.append(exercise.getEquipment());
        }
        if (exercise.getCompoundOrIsolation() != null && !exercise.getCompoundOrIsolation().isEmpty()) {
            if (details.length() > 0) details.append(" • ");
            details.append(exercise.getCompoundOrIsolation());
        }
        if (exercise.getDifficulty() != null && !exercise.getDifficulty().isEmpty()) {
            if (details.length() > 0) details.append(" • ");
            details.append(exercise.getDifficulty());
        }

        if (holder.textDetails != null) {
            if (details.length() > 0) {
                holder.textDetails.setText(details.toString());
                holder.textDetails.setVisibility(View.VISIBLE);
            } else {
                holder.textDetails.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExercisePicked(exercise);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    // ── ViewHolder ──────────────────────────────────────────────────────

    static class PickViewHolder extends RecyclerView.ViewHolder {
        final TextView textName;
        final TextView textMuscleGroup;
        final TextView textDetails;

        PickViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textPickExerciseName);
            textMuscleGroup = itemView.findViewById(R.id.textPickMuscleGroup);
            textDetails = itemView.findViewById(R.id.textPickDetails);
        }
    }
}
