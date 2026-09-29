package com.example.myroutine.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Adapter for the calendar's day-detail workout list.
 * Shows each workout's name, time, calories, and duration.
 */
public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.ViewHolder> {

    private List<Workout> workouts = new ArrayList<>();

    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts != null ? workouts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_workout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Workout w = workouts.get(position);

        holder.textName.setText(w.getName());

        // Time
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        holder.textTime.setText(sdf.format(new Date(w.getTimestamp())));

        // Calories
        if (w.getCaloriesBurnt() > 0) {
            holder.textCalories.setText(String.format(Locale.getDefault(), "🔥 %.0f cal", w.getCaloriesBurnt()));
            holder.textCalories.setVisibility(View.VISIBLE);
        } else {
            holder.textCalories.setVisibility(View.GONE);
        }

        // Duration
        long mins = w.getDurationSeconds() / 60;
        if (mins > 0) {
            holder.textDuration.setText(mins + " min");
        } else {
            holder.textDuration.setText("");
        }
    }

    @Override
    public int getItemCount() { return workouts.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textName, textTime, textCalories, textDuration;
        ViewHolder(@NonNull View v) {
            super(v);
            textName = v.findViewById(R.id.textCalWorkoutName);
            textTime = v.findViewById(R.id.textCalWorkoutTime);
            textCalories = v.findViewById(R.id.textCalCalories);
            textDuration = v.findViewById(R.id.textCalDuration);
        }
    }
}
