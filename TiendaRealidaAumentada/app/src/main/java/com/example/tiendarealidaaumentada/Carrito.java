package com.example.tiendarealidaaumentada;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.app.AlertDialog;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.tiendarealidaaumentada.Adapter.GenericAdapter;
import com.example.tiendarealidaaumentada.models.Producto;

import java.util.ArrayList;

public class Carrito extends AppCompatActivity {

    private RecyclerView recyclerCarrito;
    private GenericAdapter<Producto> carritoAdapter;
    private ArrayList<Producto> carrito;
    private TextView txtTotal,txtSubtotal, txtImpuesto;
    private Button btnComprar;
    private BottomNavigationView bottomNavigationView;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carrito);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        recyclerCarrito = findViewById(R.id.recyclerCarrito);
        txtTotal = findViewById(R.id.txtTotal);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtImpuesto = findViewById(R.id.txtImpuestos);
        btnComprar = findViewById(R.id.btnComprar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.itemCarrito);

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
            precio.setText("$" + String.format("%.2f", producto.getPrecio())); // Mostrar precio unitario
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
        btnComprar.setOnClickListener(v -> mostrarDialogoConfirmacion());
        calcularSubTotal();
        calcularImpuestoIVA();
        calcularTotal();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.itemHome) {
                Intent intent = new Intent(Carrito.this, Home.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.itemCarrito) {
                /*Intent intent = new Intent(Carrito.this, Carrito.class);
                startActivity(intent);*/
                return true;
            } else if (itemId == R.id.itemHistorial) {
                Intent intent = new Intent(Carrito.this, HistorialCompra.class);
                startActivity(intent);
                return true;
            } else {
                return false;
            }
        });
    }

    private void calcularSubTotal(){
        double subtotal = 0;
        for (Producto p : carrito) {
            subtotal += p.getPrecioTotal(); // Usar precio total (precio * cantidad)
        }
        txtSubtotal.setText("$" + String.format("%.2f", subtotal));
    }

    private void calcularImpuestoIVA() {
        double tasaIVA = 0.13;
        double totalIVA = 0.0;

        for (Producto p : carrito) {
            double ivaProducto = p.getPrecioTotal() * tasaIVA; // Aplicar IVA al precio total
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

    private void mostrarDialogoConfirmacion() {
        if (carrito.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Compra");
        builder.setMessage("¿Estás seguro de que deseas realizar esta compra?");

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            realizarCompra();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void realizarCompra() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear un ID único para la compra basado en timestamp
        String compraId = String.valueOf(System.currentTimeMillis());
        String userId = user.getUid(); // Usar UID en lugar de email para mayor seguridad

        // Obtener fecha y hora actual
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String fechaHora = sdf.format(new Date());

        // Calcular total
        double total = 0.0;
        double tasaIVA = 0.13;
        for (Producto p : carrito) {
            total += p.getPrecioTotal() * (1 + tasaIVA);
        }

        // Crear objeto de compra
        Map<String, Object> compra = new HashMap<>();
        compra.put("fecha", fechaHora);
        compra.put("total", total);

        // Crear lista de productos
        List<Map<String, Object>> productosCompra = new ArrayList<>();
        for (Producto producto : carrito) {
            Map<String, Object> productoData = new HashMap<>();
            productoData.put("nombre", producto.getNombre());
            productoData.put("precio", producto.getPrecio());
            productoData.put("cantidad", producto.getCantidad());
            productosCompra.add(productoData);
        }
        compra.put("productos", productosCompra);

        // Guardar en Firebase
        mDatabase.child("compras").child(userId).child(compraId)
                .setValue(compra)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mostrarDialogoExito();
                    } else {
                        Toast.makeText(Carrito.this, "Error al guardar la compra: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void mostrarDialogoExito() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¡Compra Realizada!");
        builder.setMessage("Tu compra se ha realizado con éxito.");
        builder.setCancelable(false);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            // Limpiar carrito
            carrito.clear();
            carritoAdapter.updateData(carrito);

            // Actualizar totales
            calcularSubTotal();
            calcularImpuestoIVA();
            calcularTotal();

            // Redirigir a Home
            Intent intent = new Intent(Carrito.this, Home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
