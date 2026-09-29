package com.example.myroutine.ui.routines;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myroutine.R;
import com.example.myroutine.data.AppDatabase;
import com.example.myroutine.data.WorkoutRepository;
import com.example.myroutine.data.entity.Exercise;
import com.example.myroutine.data.entity.RoutineExercise;
import com.example.myroutine.data.entity.WeeklyRoutine;
import com.example.myroutine.ui.workout.ExercisePickAdapter;
import com.example.myroutine.ui.workout.ExercisePickerDialog;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Editor screen for a single day's routine.
 * <p>
 * Receives the day of week (and optional existing routine ID) via Intent extras.
 * Allows setting a routine name, toggling rest day, adding exercises with
 * target sets/reps, and saving.
 */
public class RoutineDetailActivity extends AppCompatActivity
        implements RoutineExerciseAdapter.OnRoutineExerciseListener {

    public static final String EXTRA_DAY_OF_WEEK = "day_of_week";
    public static final String EXTRA_ROUTINE_ID = "routine_id";

    private WorkoutRepository repository;
    private int dayOfWeek;
    private int existingRoutineId = -1;

    private TextInputEditText editRoutineName;
    private MaterialSwitch switchRestDay;
    private RecyclerView recyclerExercises;
    private TextView textNoExercises;
    private RoutineExerciseAdapter adapter;
    private List<RoutineExerciseAdapter.RoutineExerciseItem> exerciseItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_detail);
        com.example.myroutine.ui.ForgeUi.insets(this, ((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0));

        repository = new WorkoutRepository(getApplication());
        dayOfWeek = getIntent().getIntExtra(EXTRA_DAY_OF_WEEK, 1);
        existingRoutineId = getIntent().getIntExtra(EXTRA_ROUTINE_ID, -1);

        // ── Toolbar ─────────────────────────────────────────────────────
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(WeeklyRoutine.dayName(dayOfWeek) + " — " + getString(R.string.edit_routine));
        toolbar.setNavigationOnClickListener(v -> finish());

        // ── Views ───────────────────────────────────────────────────────
        editRoutineName = findViewById(R.id.editRoutineName);
        switchRestDay = findViewById(R.id.switchRestDay);
        recyclerExercises = findViewById(R.id.recyclerRoutineExercises);
        textNoExercises = findViewById(R.id.textNoExercisesInRoutine);
        MaterialButton btnAddExercise = findViewById(R.id.btnAddExerciseToRoutine);
        MaterialButton btnSave = findViewById(R.id.btnSaveRoutine);

        // ── Adapter ─────────────────────────────────────────────────────
        adapter = new RoutineExerciseAdapter(this);
        recyclerExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerExercises.setAdapter(adapter);

        // ── Rest day toggle ─────────────────────────────────────────────
        switchRestDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnAddExercise.setEnabled(!isChecked);
            recyclerExercises.setAlpha(isChecked ? 0.3f : 1.0f);
        });

        // ── Load existing routine ───────────────────────────────────────
        if (existingRoutineId > 0) {
            loadExistingRoutine();
        }

        // ── Add exercise button ─────────────────────────────────────────
        btnAddExercise.setOnClickListener(v -> {
            // Show the exercise picker dialog
            ExercisePickerDialogForRoutine dialog = new ExercisePickerDialogForRoutine();
            dialog.setOnExerciseSelected(exercise -> {
                // Check for duplicates
                for (RoutineExerciseAdapter.RoutineExerciseItem item : exerciseItems) {
                    if (item.exerciseId == exercise.getId()) return;
                }
                exerciseItems.add(new RoutineExerciseAdapter.RoutineExerciseItem(
                        exercise.getId(), exercise.getName(), 3, 10));
                adapter.setItems(exerciseItems);
                updateEmptyState();
            });
            dialog.show(getSupportFragmentManager(), "routine_exercise_picker");
        });

        // ── Save button ────────────────────────────────────────────────
        btnSave.setOnClickListener(v -> saveRoutine());

        updateEmptyState();
    }

    private void loadExistingRoutine() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            WeeklyRoutine routine = repository.getRoutineById(existingRoutineId);
            if (routine != null) {
                runOnUiThread(() -> {
                    editRoutineName.setText(routine.getRoutineName());
                    switchRestDay.setChecked(routine.isRestDay());
                });

                // Load exercises
                List<RoutineExercise> reList = repository.getExercisesForRoutineSync(existingRoutineId);
                List<RoutineExerciseAdapter.RoutineExerciseItem> items = new ArrayList<>();
                for (RoutineExercise re : reList) {
                    Exercise ex = repository.getExerciseById(re.getExerciseId());
                    if (ex != null) {
                        items.add(new RoutineExerciseAdapter.RoutineExerciseItem(
                                ex.getId(), ex.getName(), re.getTargetSets(), re.getTargetReps()));
                    }
                }
                runOnUiThread(() -> {
                    exerciseItems = items;
                    adapter.setItems(exerciseItems);
                    updateEmptyState();
                });
            }
        });
    }

    private void saveRoutine() {
        String name = editRoutineName.getText() != null ?
                editRoutineName.getText().toString().trim() : "";
        boolean isRest = switchRestDay.isChecked();

        if (!isRest && name.isEmpty()) {
            editRoutineName.setError(getString(R.string.field_required));
            return;
        }

        if (isRest && name.isEmpty()) {
            name = "Rest Day";
        }

        for (RoutineExerciseAdapter.RoutineExerciseItem item : exerciseItems) {
            if (!isRest && (item.targetSets < 1 || item.targetSets > 30 || item.targetReps < 1 || item.targetReps > 1000)) {
                Toast.makeText(this, "Use 1?30 sets and 1?1000 reps per exercise.", Toast.LENGTH_LONG).show();return;
            }
        }
        if (!isRest && exerciseItems.isEmpty()) {
            Toast.makeText(this, "Add an exercise or mark this as a rest day.", Toast.LENGTH_LONG).show();return;
        }
        WeeklyRoutine routine = new WeeklyRoutine(dayOfWeek, name, isRest, System.currentTimeMillis());
        List<RoutineExerciseAdapter.RoutineExerciseItem> snapshot = new ArrayList<>(exerciseItems);
        findViewById(R.id.btnSaveRoutine).setEnabled(false);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplication());
                db.runInTransaction(() -> {
                    int routineId = (int)db.weeklyRoutineDao().insert(routine);
                    List<RoutineExercise> rows = new ArrayList<>();
                    if (!isRest) for (int i=0;i<snapshot.size();i++) {
                        RoutineExerciseAdapter.RoutineExerciseItem item=snapshot.get(i);
                        rows.add(new RoutineExercise(routineId,item.exerciseId,item.targetSets,item.targetReps,i));
                    }
                    db.routineExerciseDao().insertAll(rows);
                });
                runOnUiThread(() -> {Toast.makeText(this,R.string.routine_saved,Toast.LENGTH_SHORT).show();finish();});
            } catch (Exception e) {
                runOnUiThread(() -> {findViewById(R.id.btnSaveRoutine).setEnabled(true);Toast.makeText(this,"Could not save. Please try again.",Toast.LENGTH_LONG).show();});
            }
        });
    }

    private void updateEmptyState() {
        boolean hasExercises = !exerciseItems.isEmpty();
        recyclerExercises.setVisibility(hasExercises ? RecyclerView.VISIBLE : RecyclerView.GONE);
        textNoExercises.setVisibility(hasExercises ? TextView.GONE : TextView.VISIBLE);
    }

    // ── RoutineExerciseAdapter callbacks ─────────────────────────────────

    @Override
    public void onRemove(int position) {
        if (position >= 0 && position < exerciseItems.size()) {
            exerciseItems.remove(position);
            adapter.setItems(exerciseItems);
            updateEmptyState();
        }
    }

    @Override
    public void onTargetSetsChanged(int position, int sets) {
        // Already updated in adapter's data
    }

    @Override
    public void onTargetRepsChanged(int position, int reps) {
        // Already updated in adapter's data
    }

    // ── Inner dialog for picking exercises ───────────────────────────────

    /**
     * A lightweight exercise picker dialog specifically for the routine editor.
     * Uses the same layout as the workout picker but talks to a local callback
     * instead of the ActiveWorkoutViewModel.
     */
    public static class ExercisePickerDialogForRoutine extends androidx.fragment.app.DialogFragment
            implements ExercisePickAdapter.OnExercisePickedListener {

        private OnExerciseSelectedCallback callback;
        private ExercisePickAdapter adapter;
        private List<Exercise> fullList = new ArrayList<>();

        public interface OnExerciseSelectedCallback {
            void onSelected(Exercise exercise);
        }

        public void setOnExerciseSelected(OnExerciseSelectedCallback callback) {
            this.callback = callback;
        }

        @NonNull
        @Override
        public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            android.view.View view = getLayoutInflater().inflate(R.layout.dialog_select_exercise, null);

            RecyclerView recycler = view.findViewById(R.id.recyclerExercisePicker);
            TextView textNoResults = view.findViewById(R.id.textNoResults);

            adapter = new ExercisePickAdapter(this);
            recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
            recycler.setAdapter(adapter);

            // Load exercises
            WorkoutRepository repo = new WorkoutRepository(requireActivity().getApplication());
            repo.getAllExercises().observe(this, exercises -> {
                fullList = exercises != null ? exercises : new ArrayList<>();
                adapter.setExercises(fullList);
                textNoResults.setVisibility(fullList.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
            });

            // Search
            TextInputEditText editSearch = view.findViewById(R.id.editSearchExercise);
            editSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(android.text.Editable s) {
                    String query = s.toString().trim().toLowerCase();
                    if (query.isEmpty()) {
                        adapter.setExercises(fullList);
                        textNoResults.setVisibility(fullList.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                    } else {
                        List<Exercise> filtered = new ArrayList<>();
                        for (Exercise e : fullList) {
                            if (matchesQuery(e, query)) {
                                filtered.add(e);
                            }
                        }
                        adapter.setExercises(filtered);
                        textNoResults.setVisibility(filtered.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                    }
                }
            });

            return new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setView(view)
                    .create();
        }

        private boolean matchesQuery(Exercise e, String query) {
            if (e == null || query == null || query.isEmpty()) return false;
            if (e.getName() != null && e.getName().toLowerCase().contains(query)) return true;
            if (e.getMuscleGroup() != null && e.getMuscleGroup().toLowerCase().contains(query)) return true;
            if (e.getEquipment() != null && e.getEquipment().toLowerCase().contains(query)) return true;
            if (e.getMovementPattern() != null && e.getMovementPattern().toLowerCase().contains(query)) return true;
            if (e.getSecondaryMuscles() != null && e.getSecondaryMuscles().toLowerCase().contains(query)) return true;
            if (e.getCompoundOrIsolation() != null && e.getCompoundOrIsolation().toLowerCase().contains(query)) return true;
            if (e.getDifficulty() != null && e.getDifficulty().toLowerCase().contains(query)) return true;
            return false;
        }

        @Override
        public void onExercisePicked(Exercise exercise) {
            if (callback != null) callback.onSelected(exercise);
            dismiss();
        }
    }
}
