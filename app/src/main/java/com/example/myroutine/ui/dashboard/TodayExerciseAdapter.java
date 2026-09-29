package com.example.myroutine.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myroutine.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the "today's exercise" preview list on the dashboard.
 * Shows exercise name and target sets × reps.
 */
public class TodayExerciseAdapter extends RecyclerView.Adapter<TodayExerciseAdapter.ViewHolder> {

    private List<DashboardViewModel.TodayExerciseItem> items = new ArrayList<>();

    public void setItems(List<DashboardViewModel.TodayExerciseItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_today_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DashboardViewModel.TodayExerciseItem item = items.get(position);
        holder.textName.setText(item.name);
        holder.textTarget.setText(item.targetSets + " × " + item.targetReps);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textName, textTarget;
        ViewHolder(@NonNull View v) {
            super(v);
            textName = v.findViewById(R.id.textTodayExerciseName);
            textTarget = v.findViewById(R.id.textTodayTarget);
        }
    }
}
