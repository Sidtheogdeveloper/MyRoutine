package com.example.myroutine.ui.workout;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myroutine.R;
import com.example.myroutine.data.entity.Exercise;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * DialogFragment that presents the exercise library for the user to pick from.
 * Supports real-time search filtering. On selection, the exercise is added
 * to the active workout via the shared {@link ActiveWorkoutViewModel}.
 */
public class ExercisePickerDialog extends DialogFragment
        implements ExercisePickAdapter.OnExercisePickedListener {

    private ActiveWorkoutViewModel viewModel;
    private ExercisePickAdapter adapter;
    private List<Exercise> fullList = new ArrayList<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflate the custom layout
        View view = getLayoutInflater().inflate(R.layout.dialog_select_exercise, null);

        // Get the shared ViewModel (scoped to the host Activity)
        viewModel = new ViewModelProvider(requireActivity()).get(ActiveWorkoutViewModel.class);

        // ── Setup RecyclerView ──────────────────────────────────────────
        RecyclerView recycler = view.findViewById(R.id.recyclerExercisePicker);
        TextView textNoResults = view.findViewById(R.id.textNoResults);

        adapter = new ExercisePickAdapter(this);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        // ── Observe exercises ───────────────────────────────────────────
        viewModel.getAllExercises().observe(this, exercises -> {
            fullList = exercises != null ? exercises : new ArrayList<>();
            adapter.setExercises(fullList);
            textNoResults.setVisibility(fullList.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // ── Search filtering ────────────────────────────────────────────
        TextInputEditText editSearch = view.findViewById(R.id.editSearchExercise);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim().toLowerCase();
                if (query.isEmpty()) {
                    adapter.setExercises(fullList);
                    textNoResults.setVisibility(fullList.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    List<Exercise> filtered = new ArrayList<>();
                    for (Exercise e : fullList) {
                        if (matchesQuery(e, query)) {
                            filtered.add(e);
                        }
                    }
                    adapter.setExercises(filtered);
                    textNoResults.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
        });

        // ── Build dialog ────────────────────────────────────────────────
        return new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .create();
    }

    // ── Exercise picked callback ────────────────────────────────────────

    @Override
    public void onExercisePicked(Exercise exercise) {
        viewModel.addExercise(exercise);
        dismiss();
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
}
