package com.example.tiendarealidaaumentada;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import com.example.tiendarealidaaumentada.Adapter.CategoriasAdapter;
import com.example.tiendarealidaaumentada.Adapter.GenericAdapter;
import com.example.tiendarealidaaumentada.Interface.CategoriaCallback;
import com.example.tiendarealidaaumentada.Interface.ProductoCallback;
import com.example.tiendarealidaaumentada.models.Producto;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
    private ArrayList<Producto> carrito = new ArrayList<>();
    private BottomNavigationView bottomNavigationView;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;

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

        // Inicializar Firebase
        initializeFirebase();


        recyclerViewCategoria = findViewById(R.id.recyclerCategorias);
        recyclerViewProductos = findViewById(R.id.recyclerProductos);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.itemHome);

        //categorias.add("Categoria 1");
        //categorias.add("Categoria 2");
        //categorias.add("Categoria 3");


        inicializarCategoria();
        Log.e("Home-D-CargarCategoria" , "Lista de categorias cargadas:  " + categorias);



        adapter = new GenericAdapter<>(new ArrayList<>(), R.layout.list_item_producto, (holder, producto) -> {
            textViewNombre = holder.itemView.findViewById(R.id.txtNombreProducto);
            textViewNombre.setText(producto.getNombre());

            textViewPrecio = holder.itemView.findViewById(R.id.txtPrecioProducto);
            textViewPrecio.setText(String.valueOf(producto.getPrecio()));

            btnAgregarCarrito = holder.itemView.findViewById(R.id.btnAgregarCarrito);
            btnAgregarCarrito.setOnClickListener(v -> {
                agregarProductoAlCarrito(producto);
            });

            btnVerAR = holder.itemView.findViewById(R.id.btnVerEnAR);
            btnVerAR.setOnClickListener(v -> {
                Intent intentAr = new Intent(Home.this, ArActivity.class);
                //intent.putExtra("producto", producto);
                startActivity(intentAr);
            });

            imageViewProducto = holder.itemView.findViewById(R.id.imageView);
            imageViewProducto.setImageResource(producto.getImagenUrl());
        });

        productos.add(new Producto("Motherboard", 10.99, R.drawable.motherboard, "Otros"));
        productos.add(new Producto("Teclado", 15.99, R.drawable.teclado, "Electronica"));

        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProductos.setAdapter(adapter);



        adapter.updateData(productos);


        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.itemHome) {
                return true;
            } else if (itemId == R.id.itemCarrito) {
                Intent intent = new Intent(Home.this, Carrito.class);
                intent.putParcelableArrayListExtra("carrito", carrito);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.itemHistorial) {
                Intent intent = new Intent(Home.this, HistorialCompra.class);
                startActivity(intent);
                return true;
            } else {
                return false;
            }
        });

        // Cargar carrito desde Firebase
        cargarCarritoDesdeFirebase();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }
    }

    private void agregarProductoAlCarrito(Producto producto) {
        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si el producto ya existe en el carrito local
        boolean productoExiste = false;
        for (Producto p : carrito) {
            if (p.getNombre().equals(producto.getNombre())) {
                p.setCantidad(p.getCantidad() + 1);
                productoExiste = true;
                break;
            }
        }

        // Si no existe, agregarlo con cantidad 1
        if (!productoExiste) {
            Producto nuevoProducto = new Producto(
                    producto.getNombre(),
                    producto.getPrecio(),
                    producto.getImagenUrl(),
                    producto.getCategoria()
            );
            nuevoProducto.setCantidad(1);
            carrito.add(nuevoProducto);
        }

        // Guardar en Firebase
        guardarCarritoEnFirebase();

        Toast.makeText(this, producto.getNombre() + " agregado al carrito", Toast.LENGTH_SHORT).show();
    }

    private void guardarCarritoEnFirebase() {
        if (userId == null) return;

        // Crear lista de productos para Firebase
        List<Map<String, Object>> productosCarrito = new ArrayList<>();
        for (Producto producto : carrito) {
            Map<String, Object> productoData = new HashMap<>();
            productoData.put("nombre", producto.getNombre());
            productoData.put("precio", producto.getPrecio());
            productoData.put("cantidad", producto.getCantidad());
            productoData.put("imagenUrl", producto.getImagenUrl());
            productoData.put("categoria", producto.getCategoria());
            productosCarrito.add(productoData);
        }

        // Guardar en Firebase
        mDatabase.child("carritos").child(userId).child("productos")
                .setValue(productosCarrito)
                .addOnFailureListener(e -> {
                    Log.e("Home", "Error al guardar carrito: " + e.getMessage());
                });
    }

    private void cargarCarritoDesdeFirebase() {
        if (userId == null) return;

        mDatabase.child("carritos").child(userId).child("productos")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        carrito.clear();

                        for (DataSnapshot productoSnapshot : dataSnapshot.getChildren()) {
                            try {
                                String nombre = productoSnapshot.child("nombre").getValue(String.class);
                                Double precio = productoSnapshot.child("precio").getValue(Double.class);
                                Integer cantidad = productoSnapshot.child("cantidad").getValue(Integer.class);
                                Integer imagenUrl = productoSnapshot.child("imagenUrl").getValue(Integer.class);
                                String categoria = productoSnapshot.child("categoria").getValue(String.class);

                                if (nombre != null && precio != null && cantidad != null && imagenUrl != null && categoria != null) {
                                    Producto producto = new Producto(nombre, precio, imagenUrl, categoria);
                                    producto.setCantidad(cantidad);
                                    carrito.add(producto);
                                }
                            } catch (Exception e) {
                                Log.e("Home", "Error al cargar producto del carrito: " + e.getMessage());
                            }
                        }

                        Log.d("Home", "Carrito cargado: " + carrito.size() + " productos");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Home", "Error al cargar carrito: " + databaseError.getMessage());
                    }
                });
    }

    public void limpiarCarritoFirebase() {
        if (userId == null) return;

        mDatabase.child("carritos").child(userId)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Home", "Carrito limpiado en Firebase");
                    } else {
                        Log.e("Home", "Error al limpiar carrito: " + task.getException().getMessage());
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar carrito cuando regrese a la actividad
        cargarCarritoDesdeFirebase();
    }


    private void CargarCategoria(CategoriaCallback callback){
        mDatabase.child("categorias")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists() || !snapshot.hasChildren()){
                            crearCategoria(callback);
                            // No llamar traerCategorias aquí, se llamará automáticamente
                            // cuando se actualice la base de datos
                        }
                        else {
                            traerCategorias(snapshot);
                            callback.onCategoriasLista(new ArrayList<>(categorias));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //Toast.makeText(Home.this, "Error al verificar categorías: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        callback.onError("Error al verificar categoria " + error.getMessage());
                    }
                });
    }

    private void crearCategoria(CategoriaCallback callback) {
        List<String> CategoriasIniciales = new ArrayList<>();
        CategoriasIniciales.add("Fantasia");
        CategoriasIniciales.add("Electronica");
        CategoriasIniciales.add("Bebida");
        CategoriasIniciales.add("WH40k");
        CategoriasIniciales.add("Otros");

        AtomicInteger contador = new AtomicInteger(0);
        int totalCategorias = CategoriasIniciales.size();
        for (String nombreCategoria : CategoriasIniciales){
            Map<String, Object> categoriaInicial = new HashMap<>();
            categoriaInicial.put("created_at", ServerValue.TIMESTAMP);
            categoriaInicial.put("name", nombreCategoria);
            categoriaInicial.put("active", true);

            mDatabase.child("categorias").child(nombreCategoria)
                    .setValue(categoriaInicial)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Home", "Categoría '" + nombreCategoria + "' creada exitosamente");

                        categorias.add(nombreCategoria);
                        if (contador.incrementAndGet() == totalCategorias){
                            callback.onCategoriasLista(categorias);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Home", "Error al crear categoría '" + nombreCategoria + "': " + e.getMessage());
                        callback.onError("Error al crear categoria "+ e.getMessage());
                    });

        }
    }

    private void traerCategorias(DataSnapshot dataSnapshot) {
        try {
            if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                for (DataSnapshot categoriaSnapshot : dataSnapshot.getChildren()) {

                    String nombreCategoria = categoriaSnapshot.child("name").getValue(String.class);
                    Boolean activa = categoriaSnapshot.child("active").getValue(Boolean.class);

                    // Solo agregar categorías activas
                    if (nombreCategoria != null && !nombreCategoria.isEmpty() &&
                            (activa == null || activa)) { // Si activa es null, asumimos true

                        if (!categorias.contains(nombreCategoria)) {
                            categorias.add(nombreCategoria);
                            System.out.println(nombreCategoria);
                        }
                    }


                    Log.d("Home", "Categoría encontrada: " + nombreCategoria +
                            ", Activa: " + activa +
                            ", Key: " + categoriaSnapshot.getKey());
                }
            }

            Log.d("Home", "Categorías cargadas: " + categorias.size());
            Log.d("Home", "Lista de categorías: " + categorias.toString());



        } catch (Exception e) {
            Log.e("Home", "Error al procesar categorías: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void inicializarCategoria(){
        CargarCategoria(new CategoriaCallback() {
            @Override
            public void onCategoriasLista(ArrayList<String> categoriasL) {
                recyclerViewCategoria.setLayoutManager(new LinearLayoutManager(Home.this, LinearLayoutManager.HORIZONTAL, false));



                categoriasAdapter = new CategoriasAdapter(categoriasL, categoriaSeleccionada -> {
                    ArrayList<Producto> productosFiltrados = new ArrayList<>();
                    for (Producto p : productos) {
                        if (p.getCategoria().equals(categoriaSeleccionada)) {
                            productosFiltrados.add(p);
                        }
                    }
                    adapter.updateData(productosFiltrados);
                });
                Log.e("Home", "onCategoriasLista:  se creo/obtuvo categoria exitosamente" );
                recyclerViewCategoria.setAdapter(categoriasAdapter);
            }

            @Override
            public void onError(String error) {
                Log.e("Home", "onCategoriasLista: error al crear/obtener categoria" );
            }
        });

    }
    private void CargaProducto(ProductoCallback callback){
        mDatabase.child("Productos").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists() && snapshot.hasChildren() ){
                            crearProducto(callback);
                        }else  {
                            traerProducto(snapshot);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void traerProducto(DataSnapshot snapshot) {
        try {
            if (snapshot.exists() && snapshot.hasChildren()) {
                for (DataSnapshot productoSnapshot : snapshot.getChildren()) {

                    String nombreCategoria = productoSnapshot.child("name").getValue(String.class);
                    Boolean activa = productoSnapshot.child("active").getValue(Boolean.class);

                    // Solo agregar categorías activas
                    if (nombreCategoria != null && !nombreCategoria.isEmpty() &&
                            (activa == null || activa)) { // Si activa es null, asumimos true

                        if (!productos.contains(nombreCategoria)) {
                            categorias.add(nombreCategoria);
                            System.out.println(nombreCategoria);
                        }
                    }


                    Log.d("Home", "Categoría encontrada: " + nombreCategoria +
                            ", Activa: " + activa +
                            ", Key: " + productoSnapshot.getKey());
                }
            }

        } catch (Exception e) {
            Log.e("Home", "traerProducto: hay un error" + e.getMessage());
        }
    }

    private void crearProducto(ProductoCallback callback) {

    }

}