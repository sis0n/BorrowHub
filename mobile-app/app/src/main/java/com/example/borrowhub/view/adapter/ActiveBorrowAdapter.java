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
import com.example.borrowhub.viewmodel.TransactionViewModel.ActiveBorrow;

import java.util.ArrayList;
import java.util.List;

public class ActiveBorrowAdapter extends RecyclerView.Adapter<ActiveBorrowAdapter.ActiveBorrowViewHolder> {

    public interface OnBorrowClickListener {
        void onBorrowClick(ActiveBorrow borrow);
    }

    private List<ActiveBorrow> borrows = new ArrayList<>();
    private final OnBorrowClickListener listener;

    public ActiveBorrowAdapter(OnBorrowClickListener listener) {
        this.listener = listener;
    }

    public void setBorrows(List<ActiveBorrow> borrows) {
        this.borrows = borrows == null ? new ArrayList<>() : borrows;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActiveBorrowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_active_borrow, parent, false);
        return new ActiveBorrowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActiveBorrowViewHolder holder, int position) {
        holder.bind(borrows.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return borrows.size();
    }

    static class ActiveBorrowViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvStudentName;
        private final TextView tvStudentNumber;
        private final TextView tvCourse;
        private final TextView tvBorrowStatus;
        private final LinearLayout llItems;
        private final TextView tvBorrowTimestamp;

        ActiveBorrowViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentNumber = itemView.findViewById(R.id.tvStudentNumber);
            tvCourse = itemView.findViewById(R.id.tvCourse);
            tvBorrowStatus = itemView.findViewById(R.id.tvBorrowStatus);
            llItems = itemView.findViewById(R.id.llItems);
            tvBorrowTimestamp = itemView.findViewById(R.id.tvBorrowTimestamp);
        }

        void bind(ActiveBorrow borrow, OnBorrowClickListener listener) {
            tvStudentName.setText(borrow.studentName);
            tvStudentNumber.setText(borrow.studentNumber);
            tvCourse.setText(borrow.course);

            tvBorrowStatus.setTextColor(
                    ContextCompat.getColor(itemView.getContext(),
                            R.color.inventory_status_borrowed_text));
            tvBorrowStatus.setBackgroundTintList(
                    ContextCompat.getColorStateList(itemView.getContext(),
                            R.color.inventory_status_borrowed_bg));

            // Populate items list dynamically
            llItems.removeAllViews();
            if (borrow.items != null) {
                for (String itemName : borrow.items) {
                    TextView tvItem = new TextView(itemView.getContext());
                    tvItem.setText(itemView.getContext().getString(
                            R.string.return_item_bullet_format, itemName));
                    tvItem.setTextColor(ContextCompat.getColor(itemView.getContext(),
                            R.color.gray_700));
                    tvItem.setTextSize(13f);
                    tvItem.setPadding(0, 2, 0, 2);
                    llItems.addView(tvItem);
                }
            }

            tvBorrowTimestamp.setText(itemView.getContext().getString(
                    R.string.return_timestamp_format,
                    borrow.formattedDateTime));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBorrowClick(borrow);
                }
            });
        }
    }
}
