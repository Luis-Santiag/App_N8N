//XD no utilizada

package com.example.lista_medica2dointento;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.widget.TextView;

public class TextSizeManager {
    private static final String PREFS_NAME = "prefs_text_size";
    private static final String KEY_SIZE = "text_size";
    public static final int SIZE_NORMAL = 16;
    public static final int SIZE_GRANDE = 20;

    public static void setTextSize(Context context, int sizeSp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_SIZE, sizeSp).apply();
    }

    public static int getTextSize(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_SIZE, SIZE_NORMAL);
    }

    public static void applyTextSize(TextView textView, Context context) {
        int size = getTextSize(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public static void applyTextSize(TextView textView, Context context, int defaultSize) {
        int size = getTextSize(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size > 0 ? size : defaultSize);
    }
}
