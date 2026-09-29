package com.example.myroutine.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myroutine.R;
import com.example.myroutine.data.entity.Workout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for workout list (used in Dashboard and Calendar).
 * Displays each workout's name, date, set count, calories badge, and delete action.
 */
public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workouts = new ArrayList<>();
    private final OnWorkoutActionListener listener;

    /**
     * Callback interface for workout item interactions.
     */
    public interface OnWorkoutActionListener {
        void onDelete(Workout workout);
        void onSetCountRequested(long workoutId, TextView textView);
    }

    public WorkoutAdapter(OnWorkoutActionListener listener) {
        this.listener = listener;
    }

    /**
     * Replace the entire dataset and refresh the list.
     */
    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);

        // Workout name
        holder.textWorkoutName.setText(workout.getName());

        // Formatted date
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd yyyy • h:mm a", Locale.getDefault());
        holder.textWorkoutDate.setText(sdf.format(new Date(workout.getTimestamp())));

        // Set count (loaded asynchronously via callback)
        holder.textSetCount.setText("…"); // Placeholder while loading
        if (listener != null) {
            listener.onSetCountRequested(workout.getId(), holder.textSetCount);
        }

        // Calories badge
        if (workout.getCaloriesBurnt() > 0) {
            holder.textCaloriesBadge.setText(String.format(Locale.getDefault(),
                    "🔥 %.0f cal", workout.getCaloriesBurnt()));
            holder.textCaloriesBadge.setVisibility(View.VISIBLE);
        } else {
            holder.textCaloriesBadge.setVisibility(View.GONE);
        }

        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(workout);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    // ── ViewHolder ──────────────────────────────────────────────────────

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        final TextView textWorkoutName;
        final TextView textWorkoutDate;
        final TextView textSetCount;
        final TextView textCaloriesBadge;
        final ImageButton btnDelete;

        WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            textWorkoutName = itemView.findViewById(R.id.textWorkoutName);
            textWorkoutDate = itemView.findViewById(R.id.textWorkoutDate);
            textSetCount = itemView.findViewById(R.id.textSetCount);
            textCaloriesBadge = itemView.findViewById(R.id.textCaloriesBadge);
            btnDelete = itemView.findViewById(R.id.btnDeleteWorkout);
        }
    }
}
