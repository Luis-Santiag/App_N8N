package com.example.lista_medica2dointento;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class PerfilDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "perfil.db";
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_PERFIL = "perfil";

    public static final String COL_ID = "id";
    public static final String COL_NOMBRES = "nombres";
    public static final String COL_TELEFONO = "telefono";
    public static final String COL_DPI = "dpi";
    public static final String COL_GRUPO_SANGUINEO = "grupo_sanguineo";
    public static final String COL_ALERGIAS = "alergias";
    public static final String COL_EMERGENCIA1 = "emergencia1";
    public static final String COL_EMERGENCIA2 = "emergencia2";
    public static final String COL_EMERGENCIA3 = "emergencia3";
    public static final String COL_TRATAMIENTOS = "tratamientos";
    public static final String COL_ANTECEDENTES_MEDICOS = "antecedentes_medicos";
    public static final String COL_ANTECEDENTES_FAMILIARES = "antecedentes_familiares";

    public PerfilDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PERFIL + " (" +
                COL_ID + " INTEGER PRIMARY KEY, " +
                COL_NOMBRES + " TEXT, " +
                COL_TELEFONO + " TEXT, " +
                COL_DPI + " TEXT, " +
                COL_GRUPO_SANGUINEO + " TEXT, " +
                COL_ALERGIAS + " TEXT, " +
                COL_EMERGENCIA1 + " TEXT, " +
                COL_EMERGENCIA2 + " TEXT, " +
                COL_EMERGENCIA3 + " TEXT, " +
                COL_TRATAMIENTOS + " TEXT, " +
                COL_ANTECEDENTES_MEDICOS + " TEXT, " +
                COL_ANTECEDENTES_FAMILIARES + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERFIL);
        onCreate(db);
    }

    public void guardarPerfil(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PERFIL, null, null); // Solo un perfil
        db.insert(TABLE_PERFIL, null, values);
        db.close();
    }

    public Cursor obtenerPerfil() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PERFIL + " LIMIT 1", null);
    }
}
