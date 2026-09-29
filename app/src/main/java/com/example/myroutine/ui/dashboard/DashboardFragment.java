package com.example.myroutine.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myroutine.R;
import com.example.myroutine.data.AppDatabase;
import com.example.myroutine.data.entity.Workout;
import com.example.myroutine.ui.home.WorkoutAdapter;
import com.example.myroutine.ui.workout.ActiveWorkoutActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;

/**
 * Gamified dashboard — the main tab of the app.
 * <p>
 * Shows greeting, streak, today's routine preview, stat cards,
 * and recent workout history.
 */
public class DashboardFragment extends Fragment implements WorkoutAdapter.OnWorkoutActionListener {

    private DashboardViewModel viewModel;
    private WorkoutAdapter recentAdapter;
    private TodayExerciseAdapter todayAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // ── Views ───────────────────────────────────────────────────────
        TextView textGreeting = view.findViewById(R.id.textGreeting);
        TextView textStreak = view.findViewById(R.id.textStreak);
        TextView textTodayRoutineName = view.findViewById(R.id.textTodayRoutineName);
        RecyclerView recyclerTodayExercises = view.findViewById(R.id.recyclerTodayExercises);
        TextView textNoRoutine = view.findViewById(R.id.textNoRoutine);
        MaterialButton btnStartToday = view.findViewById(R.id.btnStartTodaysWorkout);
        TextView textStatCalories = view.findViewById(R.id.textStatCalories);
        TextView textStatPR = view.findViewById(R.id.textStatPR);
        TextView textStatPRLabel = view.findViewById(R.id.textStatPRLabel);
        TextView textStatSetsWeek = view.findViewById(R.id.textStatSetsWeek);
        RecyclerView recyclerRecent = view.findViewById(R.id.recyclerRecentWorkouts);
        TextView textNoWorkouts = view.findViewById(R.id.textNoWorkouts);

        // ── Today's exercise list ───────────────────────────────────────
        todayAdapter = new TodayExerciseAdapter();
        recyclerTodayExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTodayExercises.setAdapter(todayAdapter);

        // ── Recent workouts list ────────────────────────────────────────
        recentAdapter = new WorkoutAdapter(this);
        recyclerRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerRecent.setAdapter(recentAdapter);

        // ── Observe user profile (for greeting) ─────────────────────────
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                String greeting;
                if (hour < 12) {
                    greeting = getString(R.string.greeting_morning, profile.getName());
                } else if (hour < 17) {
                    greeting = getString(R.string.greeting_afternoon, profile.getName());
                } else {
                    greeting = getString(R.string.greeting_evening, profile.getName());
                }
                textGreeting.setText(greeting);
            }
        });

        // ── Observe streak ──────────────────────────────────────────────
        viewModel.getStreak().observe(getViewLifecycleOwner(), s -> {
            if (s != null && s > 0) {
                textStreak.setText(getString(R.string.streak_format, s));
            } else {
                textStreak.setText(R.string.streak_none);
            }
        });

        // ── Observe today's routine ─────────────────────────────────────
        viewModel.getTodayRoutine().observe(getViewLifecycleOwner(), routine -> {
            if (routine == null) {
                textTodayRoutineName.setText(R.string.no_routine_scheduled);
                textNoRoutine.setVisibility(View.VISIBLE);
                recyclerTodayExercises.setVisibility(View.GONE);
            } else if (routine.isRestDay()) {
                textTodayRoutineName.setText(R.string.rest_day);
                textNoRoutine.setVisibility(View.GONE);
                recyclerTodayExercises.setVisibility(View.GONE);
            } else {
                textTodayRoutineName.setText(routine.getRoutineName());
                textNoRoutine.setVisibility(View.GONE);
                recyclerTodayExercises.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getTodayExercises().observe(getViewLifecycleOwner(), items -> {
            todayAdapter.setItems(items);
        });

        // ── Observe stats ───────────────────────────────────────────────
        viewModel.getLastSessionCalories().observe(getViewLifecycleOwner(), cal -> {
            textStatCalories.setText(String.valueOf(cal.intValue()));
        });

        viewModel.getPrValue().observe(getViewLifecycleOwner(), textStatPR::setText);
        viewModel.getPrLabel().observe(getViewLifecycleOwner(), textStatPRLabel::setText);

        viewModel.getSetsThisWeek().observe(getViewLifecycleOwner(), sets -> {
            textStatSetsWeek.setText(String.valueOf(sets));
        });

        // ── Observe recent workouts ─────────────────────────────────────
        viewModel.getRecentWorkouts().observe(getViewLifecycleOwner(), workouts -> {
            recentAdapter.setWorkouts(workouts);
            boolean has = workouts != null && !workouts.isEmpty();
            recyclerRecent.setVisibility(has ? View.VISIBLE : View.GONE);
            textNoWorkouts.setVisibility(has ? View.GONE : View.VISIBLE);
        });

        // ── Start Today's Workout ───────────────────────────────────────
        btnStartToday.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ActiveWorkoutActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh stats when returning from a workout
        viewModel.loadStats();
    }

    // ── WorkoutAdapter.OnWorkoutActionListener ──────────────────────────

    @Override
    public void onDelete(Workout workout) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_delete_title)
                .setMessage("Delete \"" + workout.getName() + "\"?")
                .setPositiveButton(R.string.delete_workout, (d, w) -> viewModel.deleteWorkout(workout))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onSetCountRequested(long workoutId, TextView textView) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = viewModel.getSetCount(workoutId);
            String formatted = getString(R.string.sets_format, count);
            textView.post(() -> textView.setText(formatted));
        });
    }
}
