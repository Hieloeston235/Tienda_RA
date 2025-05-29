package com.example.tiendarealidaaumentada;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendarealidaaumentada.Adapter.GenericAdapter;
import com.example.tiendarealidaaumentada.models.Compra;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialCompra extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView historialRecyclerView;
    private BottomNavigationView bottomNavigationView;

    private GenericAdapter<Compra> adapter;
    private List<Compra> comprasOriginales;
    private List<Compra> comprasFiltradas;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial_compra);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.itemHistorial);

        initializeViews();
        initializeFirebase();
        setupRecyclerView();
        setupSearchView();
        cargarHistorialCompras();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.itemHome) {
                Intent intent = new Intent(HistorialCompra.this, Home.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.itemCarrito) {
                Intent intent = new Intent(HistorialCompra.this, Carrito.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.itemHistorial) {
                return true;
            } else {
                return false;
            }
        });
    }

    private void initializeViews() {
        searchView = findViewById(R.id.searchView);
        historialRecyclerView = findViewById(R.id.recyclerHistorial);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (firebaseAuth.getCurrentUser() != null) {
            userId = firebaseAuth.getCurrentUser().getUid();
        }
    }

    private void setupRecyclerView() {
        comprasOriginales = new ArrayList<>();
        comprasFiltradas = new ArrayList<>();

        adapter = new GenericAdapter<>(comprasFiltradas, R.layout.list_item_historial, new GenericAdapter.Binder<Compra>() {
            @Override
            public void bind(GenericAdapter.GenericViewHolder holder, Compra compra) {
                TextView txtNombre = holder.itemView.findViewById(R.id.txtNombreHistorialProducto);
                TextView txtFecha = holder.itemView.findViewById(R.id.txtFechaHistorialProducto);
                TextView txtPrecio = holder.itemView.findViewById(R.id.txtPrecioHistorialProducto);

                //Mostrar nombres de productos
                String nombres = compra.getNombresProductos();
                if (nombres.length() > 40) {
                    nombres = nombres.substring(0, 37) + "...";
                }
                txtNombre.setText(nombres);

                //mostrar fecha
                txtFecha.setText(formatearFecha(compra.getFecha()));

                // Mostrar total
                txtPrecio.setText(String.format("$%.2f", compra.getTotal()));

                holder.itemView.setOnClickListener(v -> {
                    mostrarDetallesCompra(compra);
                });
            }
        });

        historialRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historialRecyclerView.setAdapter(adapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filtrarCompras(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarCompras(newText);
                return true;
            }
        });
    }

    private void cargarHistorialCompras() {
        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("compras").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        comprasOriginales.clear();

                        for (DataSnapshot compraSnapshot : dataSnapshot.getChildren()) {
                            try {
                                String compraId = compraSnapshot.getKey();
                                String fecha = compraSnapshot.child("fecha").getValue(String.class);
                                Double total = compraSnapshot.child("total").getValue(Double.class);

                                List<Compra.ProductoCompra> productos = new ArrayList<>();
                                DataSnapshot productosSnapshot = compraSnapshot.child("productos");

                                for (DataSnapshot productoSnapshot : productosSnapshot.getChildren()) {
                                    String nombre = productoSnapshot.child("nombre").getValue(String.class);
                                    Integer cantidad = productoSnapshot.child("cantidad").getValue(Integer.class);
                                    Double precio = productoSnapshot.child("precio").getValue(Double.class);

                                    if (nombre != null && cantidad != null && precio != null) {
                                        Compra.ProductoCompra producto = new Compra.ProductoCompra(nombre, precio, cantidad);
                                        productos.add(producto);
                                    }
                                }

                                if (compraId != null && fecha != null && total != null) {
                                    Compra compra = new Compra(compraId, fecha, productos, total);
                                    comprasOriginales.add(compra);
                                }

                            } catch (Exception e) {
                                Log.e("HistorialCompra", "Error al procesar compra: " + e.getMessage());
                            }
                        }

                        // Ordenar por fecha (más reciente primero)
                        Collections.sort(comprasOriginales, (c1, c2) -> c2.getFecha().compareTo(c1.getFecha()));

                        // Actualizar lista filtrada y notificar al adapter
                        comprasFiltradas.clear();
                        comprasFiltradas.addAll(comprasOriginales);
                        adapter.notifyDataSetChanged();

                        if (comprasOriginales.isEmpty()) {
                            Toast.makeText(HistorialCompra.this, "No hay compras registradas", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(HistorialCompra.this, "Error al cargar historial: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("HistorialCompra", "Error de Firebase: " + databaseError.getMessage());
                    }
                });
    }

    private void filtrarCompras(String query) {
        comprasFiltradas.clear();

        if (query == null || query.trim().isEmpty()) {
            comprasFiltradas.addAll(comprasOriginales);
        } else {
            String queryLower = query.toLowerCase().trim();
            for (Compra compra : comprasOriginales) {
                if (compra.contieneProducto(queryLower)) {
                    comprasFiltradas.add(compra);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private String formatearFecha(String fechaOriginal) {
        try {
            SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat formatoMostrar = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            Date fecha = formatoOriginal.parse(fechaOriginal);
            return formatoMostrar.format(fecha);
        } catch (ParseException e) {
            Log.e("HistorialCompra", "Error al formatear fecha: " + e.getMessage());
            return fechaOriginal;
        }
    }

    //Crear diálogo para mostrar detalles de la compra
    private void mostrarDetallesCompra(Compra compra) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detalles de Compra");

        StringBuilder detalles = new StringBuilder();
        detalles.append("Fecha: ").append(formatearFecha(compra.getFecha())).append("\n\n");
        detalles.append("Productos:\n");

        for (Compra.ProductoCompra producto : compra.getProductos()) {
            detalles.append("• ").append(producto.getNombre())
                    .append(" x").append(producto.getCantidad())
                    .append(" - $").append(String.format("%.2f", producto.getPrecio()))
                    .append(" c/u")
                    .append(" (Total: $").append(String.format("%.2f", producto.getPrecioTotal())).append(")")
                    .append("\n");
        }

        detalles.append("\nTotal: $").append(String.format("%.2f", compra.getTotal()));

        builder.setMessage(detalles.toString());
        builder.setPositiveButton("Cerrar", null);
        builder.show();
    }
}