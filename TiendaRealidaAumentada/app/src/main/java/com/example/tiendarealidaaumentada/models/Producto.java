package com.example.tiendarealidaaumentada.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Producto implements Parcelable {
    private String nombre;
    private double precio;
    private int imagenUrl;
    private String Categoria;
    private int cantidad;
    public Producto(String nombre, double precio, int imagenUrl, String Categoria) {
        this.nombre = nombre;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
        this.Categoria = Categoria;
        this.cantidad = 1;
    }

    public Producto() {
        this.cantidad = 1;
    }

    protected Producto(Parcel in) {
        nombre = in.readString();
        precio = in.readDouble();
        imagenUrl = in.readInt();
        Categoria = in.readString();
        cantidad = in.readInt();
    }

    public static final Creator<Producto> CREATOR = new Creator<Producto>() {
        @Override
        public Producto createFromParcel(Parcel in) {
            return new Producto(in);
        }

        @Override
        public Producto[] newArray(int size) {
            return new Producto[size];
        }
    };

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

    public String getCategoria() {
        return Categoria;
    }

    public void setCategoria(String Categoria) {
        this.Categoria = Categoria;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(nombre);
        parcel.writeDouble(precio);
        parcel.writeInt(imagenUrl);
        parcel.writeString(Categoria);
        parcel.writeInt(cantidad);
    }
}
