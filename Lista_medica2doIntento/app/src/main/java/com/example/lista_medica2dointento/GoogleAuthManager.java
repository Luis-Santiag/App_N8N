package com.example.lista_medica2dointento;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GoogleAuthManager {
    private static final String TAG = "GoogleAuthManager";
    private static final String PREFS_NAME = "google_auth_prefs";
    private static final String KEY_SIGNED_IN = "signed_in";
    private static final String KEY_USER_EMAIL = "user_email";

    private static final String CLIENT_ID = "984493768973-udrm67kgft5d9l7l9pgqj0gvvhj2lnd1.apps.googleusercontent.com";

    private final Context context;
    private SharedPreferences securePrefs;
    private GoogleSignInClient googleSignInClient;

    public GoogleAuthManager(Context context) {
        this.context = context.getApplicationContext();
        initializeSecurePreferences();
        initializeGoogleSignIn();
    }

    private void initializeSecurePreferences() {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            securePrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error inicializando EncryptedSharedPreferences", e);
            securePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    private void initializeGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(CLIENT_ID)  // Usar Web Client ID aquí
                .requestScopes(new Scope("https://www.googleapis.com/auth/drive.file"))
                .build();

        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public boolean isSignedIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        return account != null;
    }

    public String getAccessToken() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null && account.getIdToken() != null) {
            return account.getIdToken();
        }
        return null;
    }

    public void getFreshAccessToken(TokenCallback callback) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);

        if (account == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        // Verificar permisos
        if (!GoogleSignIn.hasPermissions(account, new Scope("https://www.googleapis.com/auth/drive.file"))) {
            Log.w(TAG, "No tiene permisos de Drive, solicitando...");
            callback.onError("Se necesitan permisos de Google Drive");
            return;
        }

        // Obtener token y correo
        String idToken = account.getIdToken();
        String email = account.getEmail(); // se obtiene el correo

        if (idToken != null && !idToken.isEmpty() && email != null) {
            callback.onSuccess(idToken, email);
        } else {
            callback.onError("No se pudo obtener el token o el correo");
        }
    }


    public GoogleSignInAccount getSignedInAccount() {
        return GoogleSignIn.getLastSignedInAccount(context);
    }

    public void startSignIn(Activity activity, int requestCode) {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, requestCode);
    }

    public void handleAuthorizationResponse(Intent data, AuthCallback callback) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                // Guardar información del usuario
                securePrefs.edit()
                        .putBoolean(KEY_SIGNED_IN, true)
                        .putString(KEY_USER_EMAIL, account.getEmail())
                        .apply();

                Log.d(TAG, "Inicio de sesión exitoso: " + account.getEmail());
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        } catch (ApiException e) {
            Log.e(TAG, "Error en Google Sign-In: " + e.getStatusCode(), e);
            if (callback != null) {
                callback.onError("Error al iniciar sesión: " + e.getMessage());
            }
        }
    }

    public void signOut() {
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            securePrefs.edit().clear().apply();
            Log.d(TAG, "Sesión cerrada");
        });
    }

    /**
     * Extrae el email del ID token JWT
     * El token contiene información del usuario codificada en Base64
     */
    public String getEmailFromToken(String idToken) {
        Log.d(TAG, "=== EXTRAYENDO EMAIL DEL TOKEN ===");

        if (idToken == null || idToken.isEmpty()) {
            Log.e(TAG, "Token es null o vacío");
            return null;
        }

        try {
            Log.d(TAG, "Token recibido (primeros 50 chars): " + idToken.substring(0, Math.min(50, idToken.length())));

            // El JWT tiene 3 partes separadas por puntos: header.payload.signature
            String[] parts = idToken.split("\\.");
            Log.d(TAG, "Token dividido en " + parts.length + " partes");

            if (parts.length < 2) {
                Log.e(TAG, "Token no tiene suficientes partes");
                return null;
            }

            // Decodificar el payload (segunda parte)
            String payload = new String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE | android.util.Base64.NO_PADDING));
            Log.d(TAG, "Payload decodificado: " + payload);

            // Extraer el email usando búsqueda de string
            if (payload.contains("\"email\":")) {
                int startIdx = payload.indexOf("\"email\":\"") + 9;
                int endIdx = payload.indexOf("\"", startIdx);
                if (startIdx > 8 && endIdx > startIdx) {
                    String email = payload.substring(startIdx, endIdx);
                    Log.d(TAG, "✅ EMAIL EXTRAÍDO EXITOSAMENTE: " + email);
                    return email;
                }
            } else {
                Log.e(TAG, "El payload no contiene el campo 'email'");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error extrayendo email del token", e);
            e.printStackTrace();
        }

        Log.e(TAG, "No se pudo extraer el email");
        return null;
    }

    public interface AuthCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface TokenCallback {
        void onSuccess(String token, String email);
        void onError(String error);
    }

}

