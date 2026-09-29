package com.example.myroutine.ui.workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myroutine.R;
import com.example.myroutine.ui.workout.ActiveWorkoutViewModel.ExerciseWithSets;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Outer RecyclerView adapter that displays exercise cards during an active workout.
 * Each card contains a nested {@link SetAdapter} for the set rows.
 */
public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<ExerciseWithSets> exerciseList = new ArrayList<>();
    private final OnExerciseActionListener listener;

    /**
     * Callback for exercise-card-level interactions.
     */
    public interface OnExerciseActionListener extends SetAdapter.OnSetActionListener {
        void onRemoveExercise(int position);
        void onAddSet(int exercisePosition);
    }

    public ExerciseAdapter(OnExerciseActionListener listener) {
        this.listener = listener;
    }

    /**
     * Replace the dataset and refresh all cards.
     */
    public void setExerciseList(List<ExerciseWithSets> list) {
        this.exerciseList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_card, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseWithSets ews = exerciseList.get(position);

        // Exercise header
        holder.textExerciseName.setText(ews.exercise.getName());
        holder.textMuscleGroup.setText(ews.exercise.getMuscleGroup());

        // Remove exercise
        holder.btnRemoveExercise.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onRemoveExercise(pos);
            }
        });

        // ── Nested set RecyclerView ─────────────────────────────────────
        SetAdapter setAdapter = new SetAdapter(ews.sets, position, listener);
        holder.recyclerSets.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerSets.setAdapter(setAdapter);

        // Add set button
        holder.btnAddSet.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onAddSet(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    // ── ViewHolder ──────────────────────────────────────────────────────

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        final TextView textExerciseName;
        final TextView textMuscleGroup;
        final ImageButton btnRemoveExercise;
        final RecyclerView recyclerSets;
        final MaterialButton btnAddSet;

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            textExerciseName = itemView.findViewById(R.id.textExerciseName);
            textMuscleGroup = itemView.findViewById(R.id.textMuscleGroup);
            btnRemoveExercise = itemView.findViewById(R.id.btnRemoveExercise);
            recyclerSets = itemView.findViewById(R.id.recyclerSets);
            btnAddSet = itemView.findViewById(R.id.btnAddSet);
        }
    }
}
