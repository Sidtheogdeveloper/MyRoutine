package com.example.myroutine.ui.routines;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myroutine.R;
import com.example.myroutine.data.entity.WeeklyRoutine;

import android.widget.TextView;

import java.util.List;

/**
 * Routines screen showing a 7-day weekly plan grid (Mon–Sun).
 * Each day card is tappable to edit its routine via {@link RoutineDetailActivity}.
 */
public class RoutinesFragment extends Fragment {

    private RoutinesViewModel viewModel;
    private DayCardAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_routines, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RoutinesViewModel.class);

        RecyclerView recyclerDays = view.findViewById(R.id.recyclerDays);
        adapter = new DayCardAdapter(item -> {
            // Open routine detail editor for this day
            Intent intent = new Intent(requireContext(), RoutineDetailActivity.class);
            intent.putExtra(RoutineDetailActivity.EXTRA_DAY_OF_WEEK, item.dayOfWeek);
            if (item.hasRoutine()) {
                intent.putExtra(RoutineDetailActivity.EXTRA_ROUTINE_ID, item.routine.getId());
            }
            startActivity(intent);
        });
        recyclerDays.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerDays.setAdapter(adapter);

        viewModel.getWeeklyPlan().observe(getViewLifecycleOwner(), adapter::setItems);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadWeeklyPlan(); // Refresh after editing a routine
    }

    // ── Inner DayCard adapter ───────────────────────────────────────────

    /**
     * Simple adapter for the 7-day card list.
     */
    private static class DayCardAdapter extends RecyclerView.Adapter<DayCardAdapter.ViewHolder> {

        interface OnDayClickListener {
            void onClick(RoutinesViewModel.DayRoutineItem item);
        }

        private List<RoutinesViewModel.DayRoutineItem> items = List.of();
        private final OnDayClickListener listener;

        DayCardAdapter(OnDayClickListener listener) {
            this.listener = listener;
        }

        void setItems(List<RoutinesViewModel.DayRoutineItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_day_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RoutinesViewModel.DayRoutineItem item = items.get(position);
            holder.textDay.setText(item.getDayName());

            if (item.isRestDay()) {
                holder.textRoutine.setText(R.string.rest_day);
                holder.textRoutine.setTextColor(holder.itemView.getContext().getColor(R.color.rest_day_text));
            } else if (item.hasRoutine()) {
                holder.textRoutine.setText(item.routine.getRoutineName());
                holder.textRoutine.setTextColor(holder.itemView.getContext().getColor(R.color.primary_dark));
            } else {
                holder.textRoutine.setText(R.string.tap_to_set_up);
                holder.textRoutine.setTextColor(holder.itemView.getContext().getColor(R.color.empty_state_text));
            }

            holder.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView textDay, textRoutine;
            ViewHolder(@NonNull View v) {
                super(v);
                textDay = v.findViewById(R.id.textDayName);
                textRoutine = v.findViewById(R.id.textRoutineName);
            }
        }
    }
}
