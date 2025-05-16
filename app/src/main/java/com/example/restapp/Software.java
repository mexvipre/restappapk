package com.example.restapp;

public class Software {
    private int id;
    private String nombre;
    private String descripcion;
    private String versionsoft;  // Nueva
    private String espaciomb;    // Nueva
    private String precio;       // Nueva

    public Software(int id, String nombre, String descripcion, String versionsoft, String espaciomb, String precio) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.versionsoft = versionsoft;
        this.espaciomb = espaciomb;
        this.precio = precio;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getVersionsoft() {
        return versionsoft;
    }

    public String getEspaciomb() {
        return espaciomb;
    }

    public String getPrecio() {
        return precio;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setVersionsoft(String versionsoft) {
        this.versionsoft = versionsoft;
    }

    public void setEspaciomb(String espaciomb) {
        this.espaciomb = espaciomb;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }
}
