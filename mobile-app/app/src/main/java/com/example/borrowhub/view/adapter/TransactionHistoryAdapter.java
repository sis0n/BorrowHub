package com.example.borrowhub.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.borrowhub.R;
import com.example.borrowhub.data.remote.dto.BorrowRecordDTO;
import com.example.borrowhub.data.remote.dto.ItemDTO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.HistoryViewHolder> {

    private List<BorrowRecordDTO> records = new ArrayList<>();

    public void setRecords(List<BorrowRecordDTO> records) {
        this.records = records != null ? records : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_history_card, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(records.get(position));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStudentName;
        private final TextView tvStatus;
        private final TextView tvStudentNumber;
        private final TextView tvCourse;
        private final LinearLayout llItems;
        private final TextView tvBorrowedAt;
        private final TextView tvReturnedAt;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvHistoryStudentName);
            tvStatus = itemView.findViewById(R.id.tvHistoryStatus);
            tvStudentNumber = itemView.findViewById(R.id.tvHistoryStudentNumber);
            tvCourse = itemView.findViewById(R.id.tvHistoryCourse);
            llItems = itemView.findViewById(R.id.llHistoryItems);
            tvBorrowedAt = itemView.findViewById(R.id.tvHistoryBorrowedAt);
            tvReturnedAt = itemView.findViewById(R.id.tvHistoryReturnedAt);
        }

        void bind(BorrowRecordDTO record) {
            if (record.getStudent() != null) {
                tvStudentName.setText(record.getStudent().getName());
                tvStudentNumber.setText(record.getStudent().getStudentNumber());
                tvCourse.setText(record.getStudent().getCourse());
            } else {
                tvStudentName.setText(R.string.return_borrowed_label);
                tvStudentNumber.setText("");
                tvCourse.setText("");
            }

            String status = record.getStatus();
            tvStatus.setText(status != null ? capitalize(status) : "");

            boolean isBorrowed = "borrowed".equalsIgnoreCase(status);
            int textColorRes = isBorrowed
                    ? R.color.inventory_status_borrowed_text
                    : R.color.inventory_status_available_text;
            int bgColorRes = isBorrowed
                    ? R.color.inventory_status_borrowed_bg
                    : R.color.inventory_status_available_bg;

            tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), textColorRes));
            tvStatus.setBackgroundTintList(
                    ContextCompat.getColorStateList(itemView.getContext(), bgColorRes));

            // Items
            llItems.removeAllViews();
            if (record.getItems() != null) {
                for (ItemDTO item : record.getItems()) {
                    TextView tv = new TextView(itemView.getContext());
                    tv.setText(itemView.getContext().getString(
                            R.string.return_item_bullet_format, item.getName()));
                    tv.setTextColor(ContextCompat.getColor(itemView.getContext(),
                            android.R.color.tab_indicator_text));
                    tv.setTextSize(13f);
                    llItems.addView(tv);
                }
            }

            tvBorrowedAt.setText(itemView.getContext().getString(
                    R.string.transaction_history_borrowed_label,
                    formatDate(record.getBorrowedAt())));

            String returnedAt = record.getReturnedAt();
            if (returnedAt != null && !returnedAt.isEmpty()) {
                tvReturnedAt.setText(itemView.getContext().getString(
                        R.string.transaction_history_returned_label,
                        formatDate(returnedAt)));
                tvReturnedAt.setVisibility(View.VISIBLE);
            } else {
                tvReturnedAt.setVisibility(View.GONE);
            }
        }

        private String formatDate(String rawDate) {
            if (rawDate == null || rawDate.isEmpty()) return "-";
            SimpleDateFormat phFormatter = new SimpleDateFormat("MMM d, yyyy, hh:mma", Locale.US);
            phFormatter.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
            try {
                // Normalize microseconds to milliseconds for SimpleDateFormat compatibility
                String normalized = rawDate;
                if (normalized.contains(".") && normalized.endsWith("Z")) {
                    int dotIndex = normalized.lastIndexOf('.');
                    String fraction = normalized.substring(dotIndex + 1, normalized.length() - 1);
                    if (fraction.length() > 3) {
                        fraction = fraction.substring(0, 3);
                    }
                    normalized = normalized.substring(0, dotIndex + 1) + fraction + "Z";
                }
                SimpleDateFormat isoParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                isoParser.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = isoParser.parse(normalized);
                if (date != null) return phFormatter.format(date);
            } catch (Exception e) {
                try {
                    SimpleDateFormat isoShort = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                    isoShort.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = isoShort.parse(rawDate);
                    if (date != null) return phFormatter.format(date);
                } catch (Exception ignored) {}
            }
            return rawDate;
        }

        private String capitalize(String value) {
            if (value == null || value.isEmpty()) return value;
            return Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase(Locale.US);
        }
    }
}
