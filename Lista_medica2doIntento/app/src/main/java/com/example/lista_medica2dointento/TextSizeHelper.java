//Clase ya no utilizada pero dejada por miedo a romper algoxd

package com.example.lista_medica2dointento;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextSizeHelper {
    private static final String PREF_NAME = "TextSizePrefs";
    private static final String KEY_TEXT_SIZE = "text_size";
    private static final float TAMANIO_NORMAL = 1.0f;
    private static final float TAMANIO_GRANDE = 1.25f;

    public static void setTamanoTextoGrande(Activity activity, boolean grande) {
        SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_TEXT_SIZE, grande).apply();

        float escala = grande ? TAMANIO_GRANDE : TAMANIO_NORMAL;
        ajustarTamanoTexto((ViewGroup) activity.getWindow().getDecorView(), escala);
    }

    public static boolean isTamanoTextoGrande(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_TEXT_SIZE, false);
    }

    public static void aplicarTamanoGuardado(Activity activity) {
        boolean grande = isTamanoTextoGrande(activity);
        float escala = grande ? TAMANIO_GRANDE : TAMANIO_NORMAL;
        ajustarTamanoTexto((ViewGroup) activity.getWindow().getDecorView(), escala);
    }

    private static void ajustarTamanoTexto(ViewGroup viewGroup, float escala) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);

            if (child instanceof TextView) {
                float tamanoOriginal = ((TextView) child).getTextSize();
                ((TextView) child).setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,
                    tamanoOriginal * escala);
            }

            if (child instanceof ViewGroup) {
                ajustarTamanoTexto((ViewGroup) child, escala);
            }
        }
    }

    public static void ajustarTamanoFragment(View rootView, boolean grande) {
        float escala = grande ? TAMANIO_GRANDE : TAMANIO_NORMAL;
        if (rootView instanceof ViewGroup) {
            ajustarTamanoTexto((ViewGroup) rootView, escala);
        }
    }
}
