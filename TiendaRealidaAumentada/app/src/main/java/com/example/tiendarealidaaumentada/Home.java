package com.example.tiendarealidaaumentada;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendarealidaaumentada.Adapter.CategoriasAdapter;
import com.example.tiendarealidaaumentada.Adapter.GenericAdapter;

import java.util.ArrayList;

public class Home extends AppCompatActivity {

    private RecyclerView recyclerViewCategoria;
    private CategoriasAdapter categoriasAdapter;
    private ArrayList<String> categorias = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerViewCategoria = findViewById(R.id.recyclerCategorias);
        categorias.add("Categoria 1");
        categorias.add("Categoria 2");
        categorias.add("Categoria 3");

        recyclerViewCategoria.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoriasAdapter = new CategoriasAdapter(categorias);
        recyclerViewCategoria.setAdapter(categoriasAdapter);

    }
}