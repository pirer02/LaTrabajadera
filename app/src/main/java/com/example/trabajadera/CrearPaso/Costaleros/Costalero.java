package com.example.trabajadera.CrearPaso.Costaleros;

import java.io.Serializable;

public class Costalero implements Serializable {
    private String id;
    private String nombre;
    private String apellido;
    private double altura;          // Firestore: "altura"
    private double suplementos;
    private boolean asistencia;  // pasar lista (presente)

    private int fila;            // -1 si vacío
    private int columna;         // -1 si vacío
    private int posicionAbs;     // -1 si vacío

    public Costalero() { }

    public Costalero(String nombre, String apellido, double altura) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.altura = altura;
        this.suplementos = 0;
        this.asistencia = false;
        this.fila = -1;
        this.columna = -1;
        this.posicionAbs = -1;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public double getAltura() { return altura; }
    public void setAltura(double altura) { this.altura = altura; }

    public double getSuplementos() { return suplementos; }
    public void setSuplementos(double suplementos) { this.suplementos = suplementos; }

    public boolean isAsistencia() { return asistencia; }
    public void setAsistencia(boolean asistencia) { this.asistencia = asistencia; }

    public double getAlturaTotal() { return altura + suplementos; }

    public void addSuplemento() { if (suplementos < 15) suplementos++; }
    public void removeSuplemento() { if (suplementos > 0) suplementos--; }

    public int getFila() { return fila; }
    public void setFila(int fila) { this.fila = fila; }

    public int getColumna() { return columna; }
    public void setColumna(int columna) { this.columna = columna; }

    public int getPosicionAbs() { return posicionAbs; }
    public void setPosicionAbs(int posicionAbs) { this.posicionAbs = posicionAbs; }
}
