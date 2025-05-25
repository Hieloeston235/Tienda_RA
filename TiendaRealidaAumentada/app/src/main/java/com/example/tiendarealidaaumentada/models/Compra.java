package com.example.tiendarealidaaumentada.models;

import java.util.List;

public class Compra {
    private String id;
    private String fecha;
    private List<ProductoCompra> productos;
    private double total;

    public Compra() {

    }

    public Compra(String id, String fecha, List<ProductoCompra> productos, double total) {
        this.id = id;
        this.fecha = fecha;
        this.productos = productos;
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public List<ProductoCompra> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoCompra> productos) {
        this.productos = productos;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getNombresProductos() {
        if (productos == null || productos.isEmpty()) {
            return "Sin productos";
        }

        StringBuilder nombres = new StringBuilder();
        for (int i = 0; i < productos.size(); i++) {
            ProductoCompra producto = productos.get(i);
            nombres.append(producto.getNombre());
            if (producto.getCantidad() > 1) {
                nombres.append(" x").append(producto.getCantidad());
            }
            if (i < productos.size() - 1) {
                nombres.append(", ");
            }
        }
        return nombres.toString();
    }

    public boolean contieneProducto(String busqueda) {
        if (productos == null) return false;

        String busquedaLower = busqueda.toLowerCase();
        for (ProductoCompra producto : productos) {
            if (producto.getNombre().toLowerCase().contains(busquedaLower)) {
                return true;
            }
        }
        return false;
    }

    public static class ProductoCompra {
        private String nombre;
        private double precio;
        private int cantidad;

        public ProductoCompra() {

        }

        public ProductoCompra(String nombre, double precio, int cantidad) {
            this.nombre = nombre;
            this.precio = precio;
            this.cantidad = cantidad;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public double getPrecio() {
            return precio;
        }

        public void setPrecio(double precio) {
            this.precio = precio;
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }

        public double getPrecioTotal() {
            return precio * cantidad;
        }
    }
}