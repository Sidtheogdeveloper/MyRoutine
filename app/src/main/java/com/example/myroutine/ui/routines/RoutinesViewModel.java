package com.example.myroutine.ui.routines;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myroutine.data.AppDatabase;
import com.example.myroutine.data.WorkoutRepository;
import com.example.myroutine.data.entity.WeeklyRoutine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel for the Routines screen (weekly plan grid).
 * <p>
 * Provides a list of 7 day entries (Mon–Sun), each with the active
 * routine or null if not set. Also handles routine insertion/deletion.
 */
public class RoutinesViewModel extends AndroidViewModel {

    private final WorkoutRepository repository;

    /** Processed list of 7 items: one per day, with the active routine. */
    private final MutableLiveData<List<DayRoutineItem>> weeklyPlan = new MutableLiveData<>(new ArrayList<>());

    public RoutinesViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutRepository(application);
        loadWeeklyPlan();
    }

    public LiveData<List<DayRoutineItem>> getWeeklyPlan() {
        return weeklyPlan;
    }

    /**
     * Reload the weekly plan from the database.
     * Should be called after returning from RoutineDetailActivity.
     */
    public void loadWeeklyPlan() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<DayRoutineItem> items = new ArrayList<>();
            for (int day = 1; day <= 7; day++) {
                WeeklyRoutine routine = repository.getActiveRoutineForDay(day);
                items.add(new DayRoutineItem(day, routine));
            }
            weeklyPlan.postValue(items);
        });
    }

    // ── Inner data class ────────────────────────────────────────────────

    public static class DayRoutineItem {
        public final int dayOfWeek; // 1=Monday ... 7=Sunday
        public final WeeklyRoutine routine; // null if not set

        public DayRoutineItem(int dayOfWeek, WeeklyRoutine routine) {
            this.dayOfWeek = dayOfWeek;
            this.routine = routine;
        }

        public String getDayName() {
            return WeeklyRoutine.dayName(dayOfWeek);
        }

        public boolean hasRoutine() {
            return routine != null;
        }

        public boolean isRestDay() {
            return routine != null && routine.isRestDay();
        }
    }
}
