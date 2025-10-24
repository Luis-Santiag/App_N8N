package com.example.lista_medica2dointento;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class N8nUploader {
    private static final String TAG = "N8nUploader";

    // URL del webhook de n8n
    private static final String WEBHOOK_URL = "https://primary-production-d141e.up.railway.app/webhook/cb1a04b1-110e-4951-8f88-f053923e60dd";

    private final OkHttpClient httpClient;

    public N8nUploader() {
        // Interceptor sencillo para logging (usa la misma API de OkHttp, no requiere dependencia extra)
        Interceptor loggingInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                try {
                    Log.d(TAG, "-- HTTP REQUEST --> " + request.method() + " " + request.url());
                    if (request.body() != null) {
                        Log.d(TAG, "Request has a body (type: " + request.body().contentType() + ")");
                    }
                } catch (Exception e) {
                    Log.w(TAG, "No se pudo loggear request: " + e.getMessage());
                }

                Response response = null;
                try {
                    response = chain.proceed(request);
                    Log.d(TAG, "<-- HTTP RESPONSE -- code=" + response.code() + " for " + request.url());
                } catch (Exception e) {
                    Log.e(TAG, "Error en conexión HTTP: " + e.getMessage());
                    throw e;
                }
                return response;
            }
        };

        this.httpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * Sube un PDF al webhook de n8n junto con metadata
     * @param pdfFile Archivo PDF a subir
     * @param tipoNota Tipo de nota (Nota Médica, Receta, Nota)
     * @param asunto Asunto de la nota
     * @param userEmail Email del usuario (opcional)
     * @param accessToken Token de acceso de Google (para que n8n lo use)
     * @param callback Callback para resultado
     */
    public void uploadPdf(@NonNull File pdfFile,
                          @NonNull String tipoNota,
                          @NonNull String asunto,
                          String userEmail,
                          String accessToken,
                          @NonNull UploadCallback callback) {

        Log.d(TAG, "========================================");
        Log.d(TAG, "INICIANDO UPLOAD A N8N");
        Log.d(TAG, "========================================");
        Log.d(TAG, "URL: " + WEBHOOK_URL);
        Log.d(TAG, "Archivo: " + pdfFile.getAbsolutePath());
        Log.d(TAG, "Existe: " + pdfFile.exists());
        Log.d(TAG, "Tamaño: " + pdfFile.length() + " bytes");
        Log.d(TAG, "Puede leer: " + pdfFile.canRead());

        if (!pdfFile.exists() || !pdfFile.canRead()) {
            String error = "El archivo PDF no existe o no se puede leer";
            Log.e(TAG, error);
            callback.onError(error);
            return;
        }

        // Intentar extraer email del accessToken si userEmail no se pasó
        if ((userEmail == null || userEmail.isEmpty()) && accessToken != null && !accessToken.isEmpty()) {
            userEmail = extractEmailFromJwt(accessToken);
            Log.d(TAG, "Email extraído del token: " + userEmail);
        }

        // Construir request multipart
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", pdfFile.getName(),
                        RequestBody.create(pdfFile, MediaType.parse("application/pdf")))
                .addFormDataPart("tipo_nota", tipoNota)
                .addFormDataPart("asunto", asunto)
                .addFormDataPart("fecha_creacion", String.valueOf(System.currentTimeMillis()))
                .addFormDataPart("nombre_archivo", pdfFile.getName());

        // Añadir email si está disponible
        if (userEmail != null && !userEmail.isEmpty()) {
            multipartBuilder.addFormDataPart("user_email", userEmail);
            Log.d(TAG, "  - user_email: " + userEmail);
        }

        // Añadir access token
        if (accessToken != null && !accessToken.isEmpty()) {
            multipartBuilder.addFormDataPart("access_token", accessToken);
            Log.d(TAG, "  - access_token: [TOKEN PRESENTE - " + accessToken.length() + " chars]");
        } else {
            Log.d(TAG, "  - access_token: NO PRESENTE");
        }

        RequestBody requestBody = multipartBuilder.build();

        Request request = new Request.Builder()
                .url(WEBHOOK_URL)
                .post(requestBody)
                .addHeader("User-Agent", "MediNotas-Android/1.0")
                .build();

        Log.d(TAG, "Enviando petición HTTP POST...");
        Log.d(TAG, "Headers: User-Agent=MediNotas-Android/1.0");

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "========================================");
                Log.e(TAG, "ERROR EN LA PETICIÓN HTTP");
                Log.e(TAG, "========================================");
                Log.e(TAG, "Mensaje: " + e.getMessage());
                Log.e(TAG, "Tipo: " + e.getClass().getName());
                Log.e(TAG, Log.getStackTraceString(e));
                callback.onError("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG, "========================================");
                Log.d(TAG, "RESPUESTA RECIBIDA DE N8N");
                Log.d(TAG, "========================================");
                Log.d(TAG, "Código HTTP: " + response.code());
                Log.d(TAG, "Mensaje: " + response.message());

                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "✓ ÉXITO - Respuesta del servidor:");
                    Log.d(TAG, responseBody);
                    Log.d(TAG, "========================================");
                    callback.onSuccess(responseBody);
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Sin detalles";
                    Log.e(TAG, "✗ ERROR - Código: " + response.code());
                    Log.e(TAG, "Cuerpo del error:");
                    Log.e(TAG, errorBody);
                    Log.e(TAG, "========================================");
                    callback.onError("Error del servidor: " + response.code() + " - " + errorBody);
                }
            }
        });
    }

    /* funcionalidad de prueba */

    /**
     * Método de prueba simple para verificar conectividad con n8n
     * Envía solo texto sin archivo para diagnosticar
     */
    public void testWebhook(@NonNull UploadCallback callback) {
        Log.d(TAG, "========================================");
        Log.d(TAG, "TEST DE CONECTIVIDAD CON N8N");
        Log.d(TAG, "========================================");
        Log.d(TAG, "URL: " + WEBHOOK_URL);

        // Crear un body JSON simple
        RequestBody requestBody = RequestBody.create(
            "{\"test\":\"prueba desde app\",\"timestamp\":" + System.currentTimeMillis() + "}",
            MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(WEBHOOK_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "MediNotas-Android/1.0")
                .build();

        Log.d(TAG, "Enviando petición de prueba JSON...");

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "========================================");
                Log.e(TAG, "ERROR EN TEST DE CONECTIVIDAD");
                Log.e(TAG, "========================================");
                Log.e(TAG, "Mensaje: " + e.getMessage());
                Log.e(TAG, Log.getStackTraceString(e));
                callback.onError("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG, "========================================");
                Log.d(TAG, "RESPUESTA DE TEST RECIBIDA");
                Log.d(TAG, "========================================");
                Log.d(TAG, "Código HTTP: " + response.code());
                Log.d(TAG, "Mensaje: " + response.message());

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Cuerpo de respuesta: " + responseBody);

                if (response.isSuccessful()) {
                    callback.onSuccess("Test exitoso: " + responseBody);
                } else {
                    callback.onError("Test falló: " + response.code() + " - " + responseBody);
                }
            }
        });
    }

    /**
     * Extrae el campo "email" del payload de un JWT (no verifica firma).
     * Devuelve null si no puede extraerlo.
     */
    public static String extractEmailFromJwt(String jwt) {
        if (jwt == null) return null;
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return null;
            String payload = parts[1];
            // Base64URL decode (NO_PADDING | NO_WRAP | URL_SAFE)
            byte[] decoded = Base64.decode(payload, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
            String json = new String(decoded, StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(json);
            if (obj.has("email")) return obj.getString("email");
            // Algunos tokens usan 'email_verified' u otros campos; si fuera necesario, extraer otro claim
            if (obj.has("sub")) return obj.optString("sub");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error decodificando JWT: " + e.getMessage());
            return null;
        }
    }

    /**
     * Callback para resultados de la subida
     */
    public interface UploadCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}
