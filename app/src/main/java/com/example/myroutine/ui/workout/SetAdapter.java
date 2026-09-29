package com.example.myroutine.ui.workout;

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
import com.example.myroutine.ui.workout.ActiveWorkoutViewModel.SetData;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Inner RecyclerView adapter for individual set rows within an exercise card.
 * Each row has inputs for reps and weight, plus a remove button.
 */
public class SetAdapter extends RecyclerView.Adapter<SetAdapter.SetViewHolder> {

    private final List<SetData> sets;
    private final int exercisePosition;
    private final OnSetActionListener listener;

    /**
     * Callback for set-level interactions.
     */
    public interface OnSetActionListener {
        void onRepsChanged(int exercisePosition, int setPosition, int reps);
        void onWeightChanged(int exercisePosition, int setPosition, double weight);
        void onRemoveSet(int exercisePosition, int setPosition);
    }

    public SetAdapter(List<SetData> sets, int exercisePosition, OnSetActionListener listener) {
        this.sets = sets;
        this.exercisePosition = exercisePosition;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_set_row, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        SetData setData = sets.get(position);

        // Set number (1-indexed)
        holder.textSetNumber.setText(String.valueOf(position + 1));

        // ── Populate fields (remove watchers first to avoid recursion) ──
        holder.clearWatchers();

        if (setData.reps > 0) {
            holder.editReps.setText(String.valueOf(setData.reps));
        } else {
            holder.editReps.setText("");
        }

        if (setData.weight > 0) {
            holder.editWeight.setText(String.valueOf(setData.weight));
        } else {
            holder.editWeight.setText("");
        }

        // ── Attach new watchers ─────────────────────────────────────────
        TextWatcher repsWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int reps = s.length() > 0 ? Integer.parseInt(s.toString()) : 0;
                    listener.onRepsChanged(exercisePosition, holder.getAdapterPosition(), reps);
                } catch (NumberFormatException ignored) {}
            }
        };
        TextWatcher weightWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double weight = s.length() > 0 ? Double.parseDouble(s.toString()) : 0.0;
                    listener.onWeightChanged(exercisePosition, holder.getAdapterPosition(), weight);
                } catch (NumberFormatException ignored) {}
            }
        };

        holder.editReps.addTextChangedListener(repsWatcher);
        holder.editWeight.addTextChangedListener(weightWatcher);
        holder.repsWatcher = repsWatcher;
        holder.weightWatcher = weightWatcher;

        // Remove set button
        holder.btnRemoveSet.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onRemoveSet(exercisePosition, pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sets.size();
    }

    // ── ViewHolder ──────────────────────────────────────────────────────

    static class SetViewHolder extends RecyclerView.ViewHolder {
        final TextView textSetNumber;
        final TextInputEditText editReps;
        final TextInputEditText editWeight;
        final ImageButton btnRemoveSet;
        TextWatcher repsWatcher;
        TextWatcher weightWatcher;

        SetViewHolder(@NonNull View itemView) {
            super(itemView);
            textSetNumber = itemView.findViewById(R.id.textSetNumber);
            editReps = itemView.findViewById(R.id.editReps);
            editWeight = itemView.findViewById(R.id.editWeight);
            btnRemoveSet = itemView.findViewById(R.id.btnRemoveSet);
        }

        /** Remove existing watchers to prevent ghost callbacks during rebind. */
        void clearWatchers() {
            if (repsWatcher != null) editReps.removeTextChangedListener(repsWatcher);
            if (weightWatcher != null) editWeight.removeTextChangedListener(weightWatcher);
        }
    }

    // ── Utility ─────────────────────────────────────────────────────────

    /**
     * Convenience base class so callers only override {@code afterTextChanged}.
     */
    private static abstract class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
