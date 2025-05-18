package com.example.tiendarealidaaumentada.models;

public class Producto {
    private String nombre;
    private double precio;
    private int imagenUrl;

    public Producto(String nombre, double precio, int imagenUrl) {
        this.nombre = nombre;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
    }

    public Producto() {
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

    public int getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(int imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}
