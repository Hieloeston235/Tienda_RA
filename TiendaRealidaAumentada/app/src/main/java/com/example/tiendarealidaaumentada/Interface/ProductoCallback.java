package com.example.tiendarealidaaumentada.Interface;

import com.example.tiendarealidaaumentada.models.Producto;

import java.util.ArrayList;

public interface ProductoCallback {
    void onProductoArray(ArrayList<Producto> productoArrayLis);
    void onError(String Error);
}
