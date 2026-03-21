package com.example.borrowhub.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.borrowhub.R;
import com.example.borrowhub.data.local.entity.RecentTransactionEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<RecentTransactionEntity> transactions = new ArrayList<>();

    public void setTransactions(List<RecentTransactionEntity> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        RecentTransactionEntity transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTransactionId;
        private final TextView tvTransactionDate;
        private final TextView tvTransactionStatus;
        private final TextView tvTransactionItem;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionId = itemView.findViewById(R.id.tvTransactionId);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionStatus = itemView.findViewById(R.id.tvTransactionStatus);
            tvTransactionItem = itemView.findViewById(R.id.tvTransactionItem);
        }

        public void bind(RecentTransactionEntity transaction) {
            tvTransactionId.setText(transaction.borrowerName);
            tvTransactionDate.setText(formatDate(transaction.date));
            tvTransactionItem.setText(transaction.itemName);

            String status = transaction.status;
            tvTransactionStatus.setText(status);

            int textColorRes;
            int bgColorRes;

            if ("Borrowed".equalsIgnoreCase(status)) {
                textColorRes = R.color.inventory_status_borrowed_text;
                bgColorRes = R.color.inventory_status_borrowed_bg;
            } else if ("Returned".equalsIgnoreCase(status)) {
                textColorRes = R.color.inventory_status_available_text;
                bgColorRes = R.color.inventory_status_available_bg;
            } else {
                textColorRes = R.color.gray_700;
                bgColorRes = R.color.gray_100;
            }

            tvTransactionStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), textColorRes));
            tvTransactionStatus.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), bgColorRes));
        }

        private String formatDate(String dateStr) {
            if (dateStr == null || dateStr.isEmpty()) return "-";
            try {
                // Common Laravel formats: "2026-03-21 14:00:00" or "2026-03-21T14:00:00.000000Z"
                String normalizedDate = dateStr;
                String inputPattern;
                
                if (dateStr.contains("T")) {
                    // Trim milliseconds if present
                    if (dateStr.contains(".")) {
                        normalizedDate = dateStr.substring(0, dateStr.indexOf("."));
                        if (dateStr.endsWith("Z")) {
                            normalizedDate += "Z";
                        }
                    }
                    inputPattern = normalizedDate.endsWith("Z") ? "yyyy-MM-dd'T'HH:mm:ss'Z'" : "yyyy-MM-dd'T'HH:mm:ss";
                } else {
                    inputPattern = "yyyy-MM-dd HH:mm:ss";
                }

                SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.US);
                inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US);
                outputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

                Date date = inputFormat.parse(normalizedDate);
                return outputFormat.format(date);
            } catch (ParseException e) {
                // Return original if parsing fails
                return dateStr;
            }
        }
    }
}
