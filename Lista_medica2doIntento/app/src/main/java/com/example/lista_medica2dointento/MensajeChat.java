package com.example.lista_medica2dointento;

import android.os.Parcel;
import android.os.Parcelable;

public class MensajeChat implements Parcelable {
    public static final int TIPO_USUARIO = 0;
    public static final int TIPO_IA = 1;
    private String texto;
    private int tipo;

    public MensajeChat(String texto, int tipo) {
        this.texto = texto;
        this.tipo = tipo;
    }

    protected MensajeChat(Parcel in) {
        texto = in.readString();
        tipo = in.readInt();
    }

    public static final Creator<MensajeChat> CREATOR = new Creator<MensajeChat>() {
        @Override
        public MensajeChat createFromParcel(Parcel in) {
            return new MensajeChat(in);
        }

        @Override
        public MensajeChat[] newArray(int size) {
            return new MensajeChat[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(texto);
        dest.writeInt(tipo);
    }

    // Getters
    public String getTexto() {
        return texto;
    }

    public int getTipo() {
        return tipo;
    }
}