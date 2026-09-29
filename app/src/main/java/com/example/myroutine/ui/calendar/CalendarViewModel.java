package com.example.myroutine.ui.calendar;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myroutine.data.WorkoutRepository;
import com.example.myroutine.data.entity.Workout;

import java.util.Calendar;
import java.util.List;

/**
 * ViewModel for the Calendar screen.
 * Provides workouts for a selected date and the current month range.
 */
public class CalendarViewModel extends AndroidViewModel {

    private final WorkoutRepository repository;
    private final MutableLiveData<List<Workout>> selectedDateWorkouts = new MutableLiveData<>();

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutRepository(application);
    }

    public LiveData<List<Workout>> getSelectedDateWorkouts() {
        return selectedDateWorkouts;
    }

    /**
     * Load workouts for a specific date.
     *
     * @param year  Calendar year
     * @param month Calendar month (0-indexed, as from CalendarView)
     * @param day   Day of month
     */
    public void loadWorkoutsForDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long endOfDay = cal.getTimeInMillis();

        // Use the LiveData variant so the UI auto-updates
        repository.getWorkoutsBetween(startOfDay, endOfDay).observeForever(workouts -> {
            selectedDateWorkouts.postValue(workouts);
        });
    }

    /** Get set count synchronously (call from bg thread). */
    public int getSetCount(long workoutId) {
        return repository.getSetCount(workoutId);
    }
}
