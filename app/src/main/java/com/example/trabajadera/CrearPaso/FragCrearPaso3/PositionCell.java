package com.example.trabajadera.CrearPaso.FragCrearPaso3;

import com.example.trabajadera.CrearPaso.Costaleros.Costalero;
import java.io.Serializable;

public class PositionCell implements Serializable {
    // Índices lógicos
    public int row; // 0-based
    public int col; // 0-based
    // Datos
    public Costalero costalero; // puede ser null si vacío

    public PositionCell(int row, int col, Costalero c) {
        this.row = row;
        this.col = col;
        this.costalero = c;
    }
}
