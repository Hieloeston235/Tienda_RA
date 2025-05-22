package com.example.tiendarealidaaumentada;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView txtTotal,txtSubtotal, txtImpuesto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carrito);

        recyclerCarrito = findViewById(R.id.recyclerCarrito);
        txtTotal = findViewById(R.id.txtTotal);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtImpuesto = findViewById(R.id.txtImpuestos);

        carrito = getIntent().getParcelableArrayListExtra("carrito");
        if (carrito == null) carrito = new ArrayList<>();

        recyclerCarrito.setLayoutManager(new LinearLayoutManager(this));

        carritoAdapter = new GenericAdapter<>(carrito, R.layout.list_item_carrito, (holder, producto) -> {
            TextView nombre = holder.itemView.findViewById(R.id.txtNomreCarritoProducto);
            TextView precio = holder.itemView.findViewById(R.id.txtPrecioCarritoProducto);
            ImageView imagen = holder.itemView.findViewById(R.id.imageViewProductoCarrito);
            Button eliminar = holder.itemView.findViewById(R.id.btnEliminarProducto);
            TextView cantidad = holder.itemView.findViewById(R.id.text_value);
            TextView increment = holder.itemView.findViewById(R.id.textButtom_increment);
            TextView decrement = holder.itemView.findViewById(R.id.textButtom_decrement);

            nombre.setText(producto.getNombre());
            precio.setText("$" + String.format("%.2f", producto.getPrecio()));  // Mostrar precio del producto
            imagen.setImageResource(producto.getImagenUrl());
            cantidad.setText(String.valueOf(producto.getCantidad())); // Mostrar cantidad del producto

            eliminar.setOnClickListener(v -> {
                carrito.remove(producto);
                carritoAdapter.updateData(carrito);
                calcularSubTotal();
                calcularImpuestoIVA();
                calcularTotal();
            });

            increment.setOnClickListener(v -> {
                producto.setCantidad(producto.getCantidad() + 1);
                cantidad.setText(String.valueOf(producto.getCantidad()));
                calcularSubTotal();
                calcularImpuestoIVA();
                calcularTotal();
            });

            decrement.setOnClickListener(v -> {
                if (producto.getCantidad() > 1) {
                    producto.setCantidad(producto.getCantidad() - 1);
                    cantidad.setText(String.valueOf(producto.getCantidad()));
                    calcularSubTotal();
                    calcularImpuestoIVA();
                    calcularTotal();
                } else {
                    Toast.makeText(Carrito.this, "La cantidad no puede ser menor a 1", Toast.LENGTH_SHORT).show();
                }
            });

        });

        recyclerCarrito.setAdapter(carritoAdapter);
        calcularSubTotal();
        calcularImpuestoIVA();
        calcularTotal();
    }

    private void calcularSubTotal(){
        double subtotal = 0;
        for (Producto p : carrito) {
            subtotal += p.getPrecioTotal();
        }
        txtSubtotal.setText("$" + String.format("%.2f", subtotal));
    }

    private void calcularImpuestoIVA() {
        double tasaIVA = 0.13;
        double totalIVA = 0.0;

        for (Producto p : carrito) {
            double ivaProducto = p.getPrecioTotal() * tasaIVA;
            totalIVA += ivaProducto;
        }

        txtImpuesto.setText("$" + String.format("%.2f", totalIVA));
    }

    private void calcularTotal() {
        double subtotal = 0.0;
        double totalIVA = 0.0;
        double tasaIVA = 0.13;

        for (Producto p : carrito) {
            double precioTotal = p.getPrecioTotal();
            subtotal += precioTotal;
            totalIVA += precioTotal * tasaIVA;
        }

        double totalFinal = subtotal + totalIVA;

        txtTotal.setText("$" + String.format("%.2f", totalFinal));
    }
}
