package com.example.myroutine.ui.routines;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myroutine.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for exercises in the routine editor (RoutineDetailActivity).
 * Each row shows exercise name, target sets/reps inputs, and a remove button.
 */
public class RoutineExerciseAdapter extends RecyclerView.Adapter<RoutineExerciseAdapter.ViewHolder> {

    private List<RoutineExerciseItem> items = new ArrayList<>();
    private final OnRoutineExerciseListener listener;

    public interface OnRoutineExerciseListener {
        void onRemove(int position);
        void onTargetSetsChanged(int position, int sets);
        void onTargetRepsChanged(int position, int reps);
    }

    public RoutineExerciseAdapter(OnRoutineExerciseListener listener) {
        this.listener = listener;
    }

    public void setItems(List<RoutineExerciseItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<RoutineExerciseItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_routine_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoutineExerciseItem item = items.get(position);
        holder.textName.setText(item.exerciseName);

        // Remove watchers before setting text
        if (holder.setsWatcher != null) holder.editSets.removeTextChangedListener(holder.setsWatcher);
        if (holder.repsWatcher != null) holder.editReps.removeTextChangedListener(holder.repsWatcher);

        holder.editSets.setText(item.targetSets > 0 ? String.valueOf(item.targetSets) : "");
        holder.editReps.setText(item.targetReps > 0 ? String.valueOf(item.targetReps) : "");

        holder.setsWatcher = new SimpleWatcher() {
            @Override public void afterTextChanged(Editable s) {
                try {
                    int val = s.length() > 0 ? Integer.parseInt(s.toString()) : 0;
                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        items.get(pos).targetSets = val;
                        listener.onTargetSetsChanged(pos, val);
                    }
                } catch (NumberFormatException ignored) {}
            }
        };
        holder.repsWatcher = new SimpleWatcher() {
            @Override public void afterTextChanged(Editable s) {
                try {
                    int val = s.length() > 0 ? Integer.parseInt(s.toString()) : 0;
                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        items.get(pos).targetReps = val;
                        listener.onTargetRepsChanged(pos, val);
                    }
                } catch (NumberFormatException ignored) {}
            }
        };
        holder.editSets.addTextChangedListener(holder.setsWatcher);
        holder.editReps.addTextChangedListener(holder.repsWatcher);

        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onRemove(pos);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textName;
        final TextInputEditText editSets, editReps;
        final ImageButton btnRemove;
        TextWatcher setsWatcher, repsWatcher;

        ViewHolder(@NonNull View v) {
            super(v);
            textName = v.findViewById(R.id.textRoutineExerciseName);
            editSets = v.findViewById(R.id.editTargetSets);
            editReps = v.findViewById(R.id.editTargetReps);
            btnRemove = v.findViewById(R.id.btnRemoveRoutineExercise);
        }
    }

    // ── Data class ──────────────────────────────────────────────────────

    public static class RoutineExerciseItem {
        public int exerciseId;
        public String exerciseName;
        public int targetSets;
        public int targetReps;

        public RoutineExerciseItem(int exerciseId, String exerciseName, int targetSets, int targetReps) {
            this.exerciseId = exerciseId;
            this.exerciseName = exerciseName;
            this.targetSets = targetSets;
            this.targetReps = targetReps;
        }
    }

    private static abstract class SimpleWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
