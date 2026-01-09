package com.example.trabajadera.PasarLista;

public class Paso {
    private String id;
    private String capataz;
    private String ciudad;
    private String hermandad;
    private String paso;
    private String tipoPaso;

    public Paso() {} // Necesario para Firestore

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCapataz() { return capataz; }
    public void setCapataz(String capataz) { this.capataz = capataz; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getHermandad() { return hermandad; }
    public void setHermandad(String hermandad) { this.hermandad = hermandad; }

    public String getPaso() { return paso; }
    public void setPaso(String paso) { this.paso = paso; }

    public String getTipoPaso() { return tipoPaso; }
    public void setTipoPaso(String tipoPaso) { this.tipoPaso = tipoPaso; }
}
