package com.example.myroutine.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myroutine.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Calendar screen showing a CalendarView and workout details for the selected date.
 */
public class CalendarFragment extends Fragment {

    private CalendarViewModel viewModel;
    private CalendarDayAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        TextView textSelectedDate = view.findViewById(R.id.textSelectedDate);
        TextView textNoWorkout = view.findViewById(R.id.textNoWorkoutOnDate);
        RecyclerView recyclerDay = view.findViewById(R.id.recyclerDayWorkouts);

        adapter = new CalendarDayAdapter();
        recyclerDay.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerDay.setAdapter(adapter);

        // Format for the selected date label
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());

        // Load today's workouts initially
        Calendar today = Calendar.getInstance();
        textSelectedDate.setText(sdf.format(today.getTime()));
        viewModel.loadWorkoutsForDate(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        );

        // Calendar date selection
        calendarView.setOnDateChangeListener((cv, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            textSelectedDate.setText(sdf.format(selected.getTime()));
            viewModel.loadWorkoutsForDate(year, month, dayOfMonth);
        });

        // Observe workouts for selected date
        viewModel.getSelectedDateWorkouts().observe(getViewLifecycleOwner(), workouts -> {
            adapter.setWorkouts(workouts);
            boolean has = workouts != null && !workouts.isEmpty();
            recyclerDay.setVisibility(has ? View.VISIBLE : View.GONE);
            textNoWorkout.setVisibility(has ? View.GONE : View.VISIBLE);
        });
    }
}
