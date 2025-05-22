package com.example.tiendarealidaaumentada.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendarealidaaumentada.R;

import java.util.ArrayList;

public class CategoriasAdapter extends RecyclerView.Adapter<CategoriasAdapter.CategoriaViewHolder> {
    private ArrayList<String> categorias;
    private OnCategoriaClickListener listener;

    public interface OnCategoriaClickListener {
        void onCategoriaClick(String categoria);
    }

    public CategoriasAdapter(ArrayList<String> categorias, OnCategoriaClickListener listener) {
        this.categorias = categorias;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_categoria, parent, false);
        return new CategoriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriaViewHolder holder, int position) {
        String categoria = categorias.get(position);
        holder.textViewCategoria.setText(categoria);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoriaClick(categoria);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    public class CategoriaViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCategoria;

        public CategoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategoria = itemView.findViewById(R.id.txtCategoria);
        }
    }
}

