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

import com.example.tiendarealidaaumentada.Adapter.GenericAdapter;
import com.example.tiendarealidaaumentada.models.Producto;

import java.util.ArrayList;

public class Carrito extends AppCompatActivity {

    private RecyclerView recyclerCarrito;
    private GenericAdapter<Producto> carritoAdapter;
    private ArrayList<Producto> carrito;
    private TextView txtTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carrito);

        recyclerCarrito = findViewById(R.id.recyclerCarrito);
        txtTotal = findViewById(R.id.txtTotal);

        carrito = getIntent().getParcelableArrayListExtra("carrito");
        if (carrito == null) carrito = new ArrayList<>();

        recyclerCarrito.setLayoutManager(new LinearLayoutManager(this));

        carritoAdapter = new GenericAdapter<>(carrito, R.layout.list_item_carrito, (holder, producto) -> {
            TextView nombre = holder.itemView.findViewById(R.id.txtNomreCarritoProducto);
            TextView precio = holder.itemView.findViewById(R.id.txtPrecioCarritoProducto);
            ImageView imagen = holder.itemView.findViewById(R.id.imageViewProductoCarrito);
            Button eliminar = holder.itemView.findViewById(R.id.btnEliminarProducto);

            nombre.setText(producto.getNombre());
            precio.setText("$" + producto.getPrecio());
            imagen.setImageResource(producto.getImagenUrl());

            eliminar.setOnClickListener(v -> {
                carrito.remove(producto);
                carritoAdapter.updateData(carrito);
                calcularTotal();
            });
        });

        recyclerCarrito.setAdapter(carritoAdapter);
        calcularTotal();
    }

    private void calcularTotal() {
        double total = 0;
        for (Producto p : carrito) {
            total += p.getPrecio();
        }
        txtTotal.setText("$" + String.format("%.2f", total));
    }
}
