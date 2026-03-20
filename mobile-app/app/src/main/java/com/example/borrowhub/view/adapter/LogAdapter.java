package com.example.borrowhub.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.borrowhub.R;
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
        private final TextView tvLogId;
        private final TextView tvLogTimestamp;
        private final TextView tvLogAction;
        private final TextView tvLogActor;
        private final TextView tvLogDetails;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLogId = itemView.findViewById(R.id.tvLogId);
            tvLogTimestamp = itemView.findViewById(R.id.tvLogTimestamp);
            tvLogAction = itemView.findViewById(R.id.tvLogAction);
            tvLogActor = itemView.findViewById(R.id.tvLogActor);
            tvLogDetails = itemView.findViewById(R.id.tvLogDetails);
        }

        void bind(LogsViewModel.LogEntry entry) {
            tvLogId.setText(String.valueOf(entry.id));
            tvLogTimestamp.setText(entry.timestamp);
            tvLogAction.setText(entry.action);
            tvLogActor.setText(entry.actor);
            tvLogDetails.setText(entry.details);

            int textColorRes;
            int bgColorRes;

            if ("Borrowed".equalsIgnoreCase(entry.action)) {
                textColorRes = R.color.inventory_status_borrowed_text;
                bgColorRes = R.color.inventory_status_borrowed_bg;
            } else if ("Returned".equalsIgnoreCase(entry.action)) {
                textColorRes = R.color.inventory_status_available_text;
                bgColorRes = R.color.inventory_status_available_bg;
            } else if ("Added".equalsIgnoreCase(entry.action)) {
                textColorRes = R.color.student_course_badge_text;
                bgColorRes = R.color.student_course_badge_bg;
            } else if ("Deleted".equalsIgnoreCase(entry.action)) {
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
