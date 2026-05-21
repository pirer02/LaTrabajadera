package com.example.trabajadera.CrearPaso;

import androidx.lifecycle.ViewModel;

import com.example.trabajadera.CrearPaso.Costaleros.Costalero;
import com.example.trabajadera.CrearPaso.FragCrearPaso3.FragCrearPaso3;

import java.util.ArrayList;
import java.util.List;

public class PasoViewModel extends ViewModel {
    // Lista temporal para construir una cuadrilla
    private final List<Costalero> listaCostaleros = new ArrayList<>();

    // Cuadrillas definitivas como asignaciones (con posiciones exactas)
    private final List<List<FragCrearPaso3.Asignacion>> cuadrillasAsignaciones = new ArrayList<>();

    private String capataz, ciudad, hermandad, paso, tipoPaso;
    private int trabajaderas, maxCostaleros;

    // Costaleros temporales
    public List<Costalero> getListaCostaleros() { return listaCostaleros; }
    public void addCostalero(Costalero c) { listaCostaleros.add(c); }
    public void clearCostaleros() { listaCostaleros.clear(); }

    //Cuadrillas definitivas (asignaciones)
    public List<List<FragCrearPaso3.Asignacion>> getCuadrillasAsignaciones() { return cuadrillasAsignaciones; }
    public void addCuadrillaAsignaciones(List<FragCrearPaso3.Asignacion> asignaciones) {
        // Permitimos máx 3 cuadrillas
        if (cuadrillasAsignaciones.size() < 3) {
            // Se guarda una copia defensiva
            cuadrillasAsignaciones.add(new ArrayList<>(asignaciones));
        }
    }
    public void clearCuadrillas() { cuadrillasAsignaciones.clear(); }

    // --- Datos del paso ---
    public void setDatosPaso(String capataz, String ciudad, String hermandad, String paso,
                             String tipoPaso, int trabajaderas, int maxCostaleros) {
        this.capataz = capataz;
        this.ciudad = ciudad;
        this.hermandad = hermandad;
        this.paso = paso;
        this.tipoPaso = tipoPaso;
        this.trabajaderas = trabajaderas;
        this.maxCostaleros = maxCostaleros;
    }

    public String getCapataz() { return capataz; }
    public String getCiudad() { return ciudad; }
    public String getHermandad() { return hermandad; }
    public String getPaso() { return paso; }
    public String getTipoPaso() { return tipoPaso; }
    public int getTrabajaderas() { return trabajaderas; }
    public int getMaxCostaleros() { return maxCostaleros; }
}
