package com.example.lista_medica2dointento;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {
    private static final int RC_GOOGLE_SIGN_IN = 100;
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_FIRST_TIME = "first_time";

    private GoogleAuthManager authManager;
    private Button btnIniciarSesionGoogle;
    private Button btnOmitir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si es la primera vez
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean(KEY_FIRST_TIME, true);

        if (!isFirstTime) {
            // No es la primera vez, ir directo a MainActivity
            irAMainActivity();
            return;
        }

        setContentView(R.layout.activity_welcome);

        authManager = new GoogleAuthManager(this);
        btnIniciarSesionGoogle = findViewById(R.id.btn_iniciar_sesion_google);
        btnOmitir = findViewById(R.id.btn_omitir);

        btnIniciarSesionGoogle.setOnClickListener(v -> iniciarSesionConGoogle());
        btnOmitir.setOnClickListener(v -> omitirYContinuar());
    }

    private void iniciarSesionConGoogle() {
        Toast.makeText(this, "Iniciando sesión con Google...", Toast.LENGTH_SHORT).show();
        authManager.startSignIn(this, RC_GOOGLE_SIGN_IN);
    }

    private void omitirYContinuar() {
        marcarComoNoEsPrimeraVez();
        Toast.makeText(this, "Puedes configurar Google Drive desde tu perfil", Toast.LENGTH_LONG).show();
        irAMainActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN && data != null) {
            authManager.handleAuthorizationResponse(data, new GoogleAuthManager.AuthCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(WelcomeActivity.this,
                            "✓ Autenticación exitosa. Tus notas se guardarán en Google Drive",
                            Toast.LENGTH_LONG).show();
                        marcarComoNoEsPrimeraVez();
                        irAMainActivity();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(WelcomeActivity.this,
                            "Error en autenticación: " + error,
                            Toast.LENGTH_LONG).show();
                        // Permitir continuar sin autenticación
                        marcarComoNoEsPrimeraVez();
                        irAMainActivity();
                    });
                }
            });
        }
    }

    private void marcarComoNoEsPrimeraVez() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_FIRST_TIME, false).apply();
    }

    private void irAMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

