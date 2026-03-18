package com.example.borrowhub.view.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.borrowhub.R;
import com.example.borrowhub.data.local.entity.ItemEntity;
import com.example.borrowhub.viewmodel.InventoryConstants;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    public interface ItemActionListener {
        void onEditItem(ItemEntity item);
        void onDeleteItem(ItemEntity item);
    }

    private final boolean compactLayout;
    private final ItemActionListener listener;
    private List<ItemEntity> items = new ArrayList<>();

    public ItemAdapter(boolean compactLayout, ItemActionListener listener) {
        this.compactLayout = compactLayout;
        this.listener = listener;
    }

    public void setItems(List<ItemEntity> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = compactLayout ? R.layout.item_inventory_card : R.layout.item_inventory_row;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvItemName;
        private final TextView tvType;
        private final TextView tvStatus;
        private final TextView tvStock;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvType = itemView.findViewById(R.id.tvType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvStock = itemView.findViewById(R.id.tvStock);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(ItemEntity item, ItemActionListener listener) {
            tvItemName.setText(item.name);
            tvType.setText(item.type);
            tvStatus.setText(item.status);
            tvStock.setText(itemView.getContext().getString(
                    R.string.inventory_stock_format,
                    item.availableQuantity,
                    item.totalQuantity
            ));

            applyStatusColor(itemView.getContext(), item.status);

            btnEdit.setOnClickListener(v -> listener.onEditItem(item));
            btnDelete.setOnClickListener(v -> listener.onDeleteItem(item));
        }

        private void applyStatusColor(Context context, String status) {
            int textColor;
            int bgColor;

            if (InventoryConstants.STATUS_AVAILABLE.equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.inventory_status_available_text);
                bgColor = ContextCompat.getColor(context, R.color.inventory_status_available_bg);
            } else if (InventoryConstants.STATUS_BORROWED.equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.inventory_status_borrowed_text);
                bgColor = ContextCompat.getColor(context, R.color.inventory_status_borrowed_bg);
            } else if (InventoryConstants.STATUS_MAINTENANCE.equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.inventory_status_maintenance_text);
                bgColor = ContextCompat.getColor(context, R.color.inventory_status_maintenance_bg);
            } else {
                textColor = ContextCompat.getColor(context, R.color.gray_700);
                bgColor = ContextCompat.getColor(context, R.color.gray_100);
            }

            tvStatus.setTextColor(textColor);
            tvStatus.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        }
    }
}
