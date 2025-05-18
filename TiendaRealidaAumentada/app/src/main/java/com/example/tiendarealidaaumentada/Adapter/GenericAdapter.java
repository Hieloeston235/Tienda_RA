package com.example.tiendarealidaaumentada.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GenericAdapter<T> extends RecyclerView.Adapter<GenericAdapter.GenericViewHolder> {

    private List<T> items;
    private int layoutId;
    private Binder<T> binder;

    public interface Binder<T> {
        void bind(GenericViewHolder holder, T item);
    }

    public GenericAdapter(List<T> items, int layoutId, Binder<T> binder) {
        this.items = items;
        this.layoutId = layoutId;
        this.binder = binder;
    }

    @NonNull
    @Override
    public GenericViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new GenericViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
        T item = items.get(position);
        binder.bind(holder, item);
    }

    @Override
    public int getItemCount() {
        return (items == null) ? 0 : items.size();
    }

    public static class GenericViewHolder extends RecyclerView.ViewHolder {
        public GenericViewHolder(View itemView) {
            super(itemView);
        }
    }

    public void updateData(List<T> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
}
