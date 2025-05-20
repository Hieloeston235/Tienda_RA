package com.example.tiendarealidaaumentada;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.tiendarealidaaumentada.models.Producto;

import java.util.ArrayList;

public class Home extends AppCompatActivity {

    private RecyclerView recyclerViewCategoria, recyclerViewProductos;
    private CategoriasAdapter categoriasAdapter;
    private ArrayList<String> categorias = new ArrayList<>();

    private Producto producto;
    private TextView textViewNombre;
    private TextView textViewPrecio;
    private Button btnAgregarCarrito, btnVerAR;
    private ImageView imageViewProducto;
    private GenericAdapter<Producto> adapter;
    private ArrayList<Producto> productos = new ArrayList<>();

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
        recyclerViewProductos = findViewById(R.id.recyclerProductos);

        categorias.add("Categoria 1");
        categorias.add("Categoria 2");
        categorias.add("Categoria 3");

        recyclerViewCategoria.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoriasAdapter = new CategoriasAdapter(categorias);
        recyclerViewCategoria.setAdapter(categoriasAdapter);

        adapter = new GenericAdapter<>(new ArrayList<>(), R.layout.list_item_producto, (holder, producto) -> {
            textViewNombre = holder.itemView.findViewById(R.id.txtNombreProducto);
            textViewNombre.setText(producto.getNombre());

            textViewPrecio = holder.itemView.findViewById(R.id.txtPrecioProducto);
            textViewPrecio.setText(String.valueOf(producto.getPrecio()));

            btnAgregarCarrito = holder.itemView.findViewById(R.id.btnAgregarCarrito);
            btnVerAR = holder.itemView.findViewById(R.id.btnVerEnAR);
            imageViewProducto = holder.itemView.findViewById(R.id.imageView);
            imageViewProducto.setImageResource(producto.getImagenUrl());
        });

        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProductos.setAdapter(adapter);

        productos.add(new Producto("Motherboard", 10.99, R.drawable.motherboard));
        productos.add(new Producto("Teclado", 15.99, R.drawable.teclado));


        adapter.updateData(productos);


    }
}