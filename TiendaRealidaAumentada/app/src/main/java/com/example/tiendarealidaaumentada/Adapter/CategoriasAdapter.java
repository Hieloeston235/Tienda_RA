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

    public CategoriasAdapter(ArrayList<String> categorias) {
        this.categorias = categorias;
    }

    @NonNull
    @Override
    public CategoriasAdapter.CategoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_categoria, parent, false);
        return new CategoriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriasAdapter.CategoriaViewHolder holder, int position) {
        String categoria = categorias.get(position);
        TextView textViewCategoria = holder.itemView.findViewById(R.id.txtCategoria);
        textViewCategoria.setText(categoria);
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    public class CategoriaViewHolder extends RecyclerView.ViewHolder {
        public CategoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            TextView textViewCategoria = itemView.findViewById(R.id.txtCategoria);
        }
    }
}
