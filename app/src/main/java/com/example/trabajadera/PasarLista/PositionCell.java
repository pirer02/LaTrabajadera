package com.example.trabajadera.PasarLista;

import com.example.trabajadera.CrearPaso.Costaleros.Costalero;

public class PositionCell {
    public int fila;
    public int columna;
    public int posicionAbs;
    public Costalero costalero; // null = vacío

    public PositionCell(int fila, int columna, int posicionAbs, Costalero c) {
        this.fila = fila;
        this.columna = columna;
        this.posicionAbs = posicionAbs;
        this.costalero = c;
    }

    public boolean isEmpty() { return costalero == null; }
}
