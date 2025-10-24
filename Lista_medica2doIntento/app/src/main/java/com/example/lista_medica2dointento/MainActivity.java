package com.example.lista_medica2dointento;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton botonEmergencia;
    private PerfilDatabaseHelper dbHelper;
    private AlertDialog dialogEmergencia;

    private PerfilFragment perfilFragment;
    private NotasFragment notasFragment;
    private CrearNotaFragment crearNotaFragment;
    private ChatFragment chatFragment;
    private MapaFragment mapaFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View rootView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewCompat.requestApplyInsets(rootView);

        dbHelper = new PerfilDatabaseHelper(this);
        inicializarVistas();
        inicializarFragmentos(savedInstanceState);
        configurarNavegacion();
        configurarBotonEmergencia();

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.perfil);
        } else {
            int selectedItemId = bottomNavigationView.getSelectedItemId();
            if (selectedItemId == 0 || getSupportFragmentManager().findFragmentById(R.id.contenedor_fragment) == null ){
                 selectedItemId = R.id.perfil; 
            }
            bottomNavigationView.setSelectedItemId(selectedItemId); 
        }
    }

    private void inicializarVistas() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        botonEmergencia = findViewById(R.id.botonEmergencia);
    }

    private void inicializarFragmentos(Bundle savedInstanceState) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        perfilFragment = (PerfilFragment) fm.findFragmentByTag("perfil");
        crearNotaFragment = (CrearNotaFragment) fm.findFragmentByTag("crear_nota");
        chatFragment = (ChatFragment) fm.findFragmentByTag("chat");
        mapaFragment = (MapaFragment) fm.findFragmentByTag("mapa");
        notasFragment = (NotasFragment) fm.findFragmentByTag("notas");

        if (perfilFragment == null) {
            perfilFragment = new PerfilFragment();
            transaction.add(R.id.contenedor_fragment, perfilFragment, "perfil");
        }
        if (crearNotaFragment == null) {
            crearNotaFragment = new CrearNotaFragment();
            transaction.add(R.id.contenedor_fragment, crearNotaFragment, "crear_nota");
        }
        if (chatFragment == null) {
            chatFragment = new ChatFragment();
            transaction.add(R.id.contenedor_fragment, chatFragment, "chat");
        }
        if (mapaFragment == null) {
            mapaFragment = new MapaFragment();
            transaction.add(R.id.contenedor_fragment, mapaFragment, "mapa");
        }

        transaction.hide(perfilFragment);
        transaction.hide(crearNotaFragment);
        transaction.hide(chatFragment);
        transaction.hide(mapaFragment);

        if (notasFragment != null && notasFragment.isAdded()) {
            transaction.hide(notasFragment);
        }
        transaction.commit();
    }

    private void configurarNavegacion() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            int itemId = item.getItemId();

            if (activeFragment != null) {
                if (activeFragment == notasFragment && notasFragment != null) { 
                    transaction.remove(notasFragment); 
                    notasFragment = null; 
                } else { 
                    transaction.hide(activeFragment);
                }
            }

            if (itemId == R.id.notas) {
                notasFragment = new NotasFragment(); 
                transaction.add(R.id.contenedor_fragment, notasFragment, "notas"); 
                activeFragment = notasFragment;
            } else if (itemId == R.id.perfil) {
                transaction.show(perfilFragment);
                activeFragment = perfilFragment;
            } else if (itemId == R.id.crear_nota) {
                transaction.show(crearNotaFragment);
                activeFragment = crearNotaFragment;
            } else if (itemId == R.id.chat) {
                transaction.show(chatFragment);
                activeFragment = chatFragment;
            } else if (itemId == R.id.mapa) {
                transaction.show(mapaFragment);
                activeFragment = mapaFragment;
            }
            transaction.commit();
            return true;
        });
    }

    private void configurarBotonEmergencia() {
        botonEmergencia.setOnClickListener(v -> mostrarDialogoEmergencia());

        // Test de n8n
        botonEmergencia.setOnLongClickListener(v -> {
            android.widget.Toast.makeText(this, "Probando conexión con n8n...", android.widget.Toast.LENGTH_SHORT).show();
            testConexionN8n();
            return true;
        });
    }

    private void mostrarDialogoEmergencia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_emergencia, null);
        Button btnBomberos = view.findViewById(R.id.btn_bomberos);
        Button btnPolicia = view.findViewById(R.id.btn_policia);
        TextView tvContactosTitulo = view.findViewById(R.id.tv_contactos_titulo);
        LinearLayout layoutContactos = view.findViewById(R.id.layout_contactos_emergencia);

        btnBomberos.setOnClickListener(v -> abrirMarcador("123"));
        btnPolicia.setOnClickListener(v -> abrirMarcador("110"));

        Cursor cursor = dbHelper.obtenerPerfil();
        if (cursor != null && cursor.moveToFirst()) {
            String emergencia1 = cursor.getString(cursor.getColumnIndexOrThrow(PerfilDatabaseHelper.COL_EMERGENCIA1));
            String emergencia2 = cursor.getString(cursor.getColumnIndexOrThrow(PerfilDatabaseHelper.COL_EMERGENCIA2));
            String emergencia3 = cursor.getString(cursor.getColumnIndexOrThrow(PerfilDatabaseHelper.COL_EMERGENCIA3));
            if (emergencia1 != null && !emergencia1.isEmpty() || emergencia2 != null && !emergencia2.isEmpty() || emergencia3 != null && !emergencia3.isEmpty()) {
                tvContactosTitulo.setVisibility(View.VISIBLE);
                if (emergencia1 != null && !emergencia1.isEmpty()) {
                    agregarBotonContacto(layoutContactos, "Contacto 1", emergencia1);
                }
                if (emergencia2 != null && !emergencia2.isEmpty()) {
                    agregarBotonContacto(layoutContactos, "Contacto 2", emergencia2);
                }
                if (emergencia3 != null && !emergencia3.isEmpty()) {
                    agregarBotonContacto(layoutContactos, "Contacto 3", emergencia3);
                }
            }
            cursor.close();
        }

        builder.setView(view);
        dialogEmergencia = builder.create();
        dialogEmergencia.show();
    }

    private void agregarBotonContacto(LinearLayout container, String nombre, String numero) {
        Button btn = new Button(this);
        btn.setText(nombre + "\n" + numero);
        btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.rojo_emergencia));
        btn.setTextColor(ContextCompat.getColor(this, R.color.blanco));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        btn.setLayoutParams(params);
        btn.setOnClickListener(v -> {
            abrirMarcador(numero);
            if (dialogEmergencia != null) {
                dialogEmergencia.dismiss();
            }
        });
        container.addView(btn);
    }

    private void abrirMarcador(String numero) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + numero));
        startActivity(intent);
    }

    private void testConexionN8n() {
        N8nUploader uploader = new N8nUploader();
        uploader.testWebhook(new N8nUploader.UploadCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(MainActivity.this,
                        "✓ n8n respondió correctamente: " + response,
                        android.widget.Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(MainActivity.this,
                        "✗ Error conectando con n8n: " + error,
                        android.widget.Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Manejar resultado de autenticación OAuth
        if (requestCode == 100 && data != null) {
            GoogleAuthManager authManager = new GoogleAuthManager(this);
            authManager.handleAuthorizationResponse(data, new GoogleAuthManager.AuthCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(MainActivity.this,
                                "✓ Autenticación exitosa con Google",
                                android.widget.Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(MainActivity.this,
                                "Error en autenticación: " + error,
                                android.widget.Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
    }
}
