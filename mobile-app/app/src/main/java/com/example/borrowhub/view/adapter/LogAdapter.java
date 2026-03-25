package com.example.borrowhub.view.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.borrowhub.R;
import com.example.borrowhub.utils.DateUtils;
import com.example.borrowhub.viewmodel.LogsViewModel;

import java.util.ArrayList;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private List<LogsViewModel.LogEntry> logs = new ArrayList<>();

    public void setLogs(List<LogsViewModel.LogEntry> logs) {
        this.logs = logs == null ? new ArrayList<>() : logs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        holder.bind(logs.get(position));
    }

    @Override
    public int getItemCount() {
        return logs == null ? 0 : logs.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLogAction;
        private final TextView tvLogTimestamp;
        private final TextView tvLogPrimary;
        private final TextView tvLogSecondary;
        private final TextView tvLogTertiary;
        private final TextView tvLogQuaternary;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLogAction = itemView.findViewById(R.id.tvLogAction);
            tvLogTimestamp = itemView.findViewById(R.id.tvLogTimestamp);
            tvLogPrimary = itemView.findViewById(R.id.tvLogPrimary);
            tvLogSecondary = itemView.findViewById(R.id.tvLogSecondary);
            tvLogTertiary = itemView.findViewById(R.id.tvLogTertiary);
            tvLogQuaternary = itemView.findViewById(R.id.tvLogQuaternary);
        }

        void bind(LogsViewModel.LogEntry entry) {
            tvLogTimestamp.setText(DateUtils.formatBackendDate(entry.timestamp));
            tvLogAction.setText(entry.action);

            if (LogsViewModel.TYPE_TRANSACTION.equals(entry.type)) {
                // Transaction Logs Layout
                tvLogPrimary.setText(entry.details);
                tvLogPrimary.setTypeface(null, Typeface.BOLD);
                tvLogPrimary.setVisibility(View.VISIBLE);

                String actionLabel = entry.action.contains("Returned") ? "Returned by: " : "Borrowed by: ";
                tvLogSecondary.setText(actionLabel + entry.target);
                tvLogSecondary.setTypeface(null, Typeface.NORMAL);
                tvLogSecondary.setVisibility(View.VISIBLE);

                tvLogTertiary.setText("Performed by: " + entry.actor);
                tvLogTertiary.setVisibility(View.VISIBLE);

                tvLogQuaternary.setVisibility(View.GONE);
            } else {
                // Activity Logs Layout
                tvLogPrimary.setVisibility(View.GONE);

                tvLogSecondary.setText("Target: " + entry.target);
                tvLogSecondary.setTypeface(null, Typeface.NORMAL);
                tvLogSecondary.setVisibility(View.VISIBLE);

                tvLogTertiary.setText("By: " + entry.actor);
                tvLogTertiary.setVisibility(View.VISIBLE);

                tvLogQuaternary.setText(entry.details);
                tvLogQuaternary.setVisibility(View.VISIBLE);
            }

            // Status Colors
            int textColorRes;
            int bgColorRes;

            if (entry.action.contains("Borrowed")) {
                textColorRes = R.color.inventory_status_borrowed_text;
                bgColorRes = R.color.inventory_status_borrowed_bg;
            } else if (entry.action.contains("Returned")) {
                textColorRes = R.color.inventory_status_available_text;
                bgColorRes = R.color.inventory_status_available_bg;
            } else if (entry.action.contains("Added")) {
                textColorRes = R.color.student_course_badge_text;
                bgColorRes = R.color.student_course_badge_bg;
            } else if (entry.action.contains("Deleted")) {
                textColorRes = R.color.inventory_status_maintenance_text;
                bgColorRes = R.color.inventory_status_maintenance_bg;
            } else {
                textColorRes = R.color.gray_700;
                bgColorRes = R.color.gray_100;
            }

            tvLogAction.setTextColor(ContextCompat.getColor(itemView.getContext(), textColorRes));
            tvLogAction.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), bgColorRes));
        }
    }
}
