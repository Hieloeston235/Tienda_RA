package com.example.tiendarealidaaumentada.Interface;

import java.util.ArrayList;

public interface CategoriaCallback{
    void onCategoriasLista(ArrayList<String> categorias);
    void onError(String error);
}