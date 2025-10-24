
package com.example.lista_medica2dointento;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotasDatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "notas.db";
    public static final int DB_VERSION = 1;

    // Tablas
    public static final String TABLA_NOTAS_MEDICAS = "notas_medicas";
    public static final String TABLA_RECETAS = "recetas";
    public static final String TABLA_NOTAS = "notas_generales";

    public NotasDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA_NOTAS_MEDICAS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, fecha TEXT, asunto TEXT, anotaciones TEXT, contenido TEXT)");
        db.execSQL("CREATE TABLE " + TABLA_RECETAS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, fecha TEXT, recetado_por TEXT, medicamentos TEXT, indicaciones TEXT)");
        db.execSQL("CREATE TABLE " + TABLA_NOTAS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, fecha TEXT, asunto TEXT, contenido TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_NOTAS_MEDICAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_RECETAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_NOTAS);
        onCreate(db);
    }
}

