package com.example.lista_medica2dointento;

public class NotaItem {
    public int id;
    public String tipo;
    public String asunto;
    public String fecha;
    public String contenido;
    public long timestamp;

    public NotaItem(int id, String tipo, String asunto, String fecha, String contenido, long timestamp) {
        this.id = id;
        this.tipo = tipo;
        this.asunto = asunto;
        this.fecha = fecha;
        this.contenido = contenido;
        this.timestamp = timestamp;
    }
}