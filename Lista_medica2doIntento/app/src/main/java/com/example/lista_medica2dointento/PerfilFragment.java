package com.example.lista_medica2dointento;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;

public class PerfilFragment extends Fragment {
    private static final int RC_GOOGLE_SIGN_IN = 100;

    private EditText etNombres, etTelefono, etDpi, etAlergias;
    private EditText etEmergencia1, etEmergencia2, etEmergencia3;
    private EditText etTratamientos, etAntecedentesMedicos, etAntecedentesFamiliares;
    private Spinner spinnerGrupoSanguineo;
    private Button btnGuardarPerfil;
    private FloatingActionButton btnEditar;
    private TextView tvGoogleDriveStatus;
    private Button btnGoogleDriveAction;
    private View rootView;
    private PerfilDatabaseHelper dbHelper;
    private GoogleAuthManager authManager;
    private boolean modoEdicion = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_perfil, container, false);
        authManager = new GoogleAuthManager(requireContext());
        inicializarVistas();
        configurarSpinnerGrupoSanguineo();
        configurarValidacionDPI();
        configurarExpansionSecciones();
        configurarGuardado();
        configurarGoogleDrive();
        actualizarEstadoGoogleDrive();

        Cursor cursor = dbHelper.obtenerPerfil();
        if (cursor != null && cursor.moveToFirst()) {
            cargarDatosGuardados(cursor);
            cursor.close();
            setModoEdicion(false); // Si hay datos, iniciar en modo no editable
        } else {
            setModoEdicion(true); // Si no hay datos, iniciar en modo editable
        }

        return rootView;
    }

    private void inicializarVistas() {
        etNombres = rootView.findViewById(R.id.et_nombres);
        etTelefono = rootView.findViewById(R.id.et_telefono);
        etDpi = rootView.findViewById(R.id.et_dpi);
        spinnerGrupoSanguineo = rootView.findViewById(R.id.spinner_grupo_sanguineo);
        etAlergias = rootView.findViewById(R.id.et_alergias);
        etEmergencia1 = rootView.findViewById(R.id.et_emergencia1);
        etEmergencia2 = rootView.findViewById(R.id.et_emergencia2);
        etEmergencia3 = rootView.findViewById(R.id.et_emergencia3);
        etTratamientos = rootView.findViewById(R.id.et_tratamientos);
        etAntecedentesMedicos = rootView.findViewById(R.id.et_antecedentes_medicos);
        etAntecedentesFamiliares = rootView.findViewById(R.id.et_antecedentes_familiares);
        btnGuardarPerfil = rootView.findViewById(R.id.btn_guardar_perfil);
        btnEditar = rootView.findViewById(R.id.btn_editar_perfil);
        tvGoogleDriveStatus = rootView.findViewById(R.id.tv_google_drive_status);
        btnGoogleDriveAction = rootView.findViewById(R.id.btn_google_drive_action);
        dbHelper = new PerfilDatabaseHelper(getContext());
        configurarValidacionTelefono(etTelefono);
        configurarValidacionTelefono(etEmergencia1);
        configurarValidacionTelefono(etEmergencia2);
        configurarValidacionTelefono(etEmergencia3);
    }

    private void configurarSpinnerGrupoSanguineo() {
        String[] tiposSangre = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                tiposSangre
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrupoSanguineo.setAdapter(adapter);
    }

    private void configurarValidacionDPI() {
        etDpi.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (input.length() > 13) {
                    s.delete(13, input.length());
                    Toast.makeText(getContext(), "El DPI no puede tener más de 13 dígitos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        etDpi.setFilters(new android.text.InputFilter[] { new android.text.InputFilter.LengthFilter(13) });
    }

    private void configurarValidacionTelefono(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (input.length() > 8) {
                    s.delete(8, input.length());
                    Toast.makeText(getContext(), "El número de teléfono debe tener 8 dígitos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        editText.setFilters(new android.text.InputFilter[] { new android.text.InputFilter.LengthFilter(8) });
    }

    private void configurarGuardado() {
        btnGuardarPerfil.setOnClickListener(v -> guardarPerfil());
        btnEditar.setOnClickListener(v -> habilitarEdicion());
    }

    private void configurarExpansionSecciones() {
        View layoutTratamientos = rootView.findViewById(R.id.layout_tratamientos);
        final EditText etTratamientosView = rootView.findViewById(R.id.et_tratamientos);
        ImageView flechaTratamientos = rootView.findViewById(R.id.flecha_tratamientos);
        if (layoutTratamientos != null && etTratamientosView != null && flechaTratamientos != null) {
            layoutTratamientos.setOnClickListener(v -> {
                boolean isVisible = etTratamientosView.getVisibility() == View.VISIBLE;
                etTratamientosView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                flechaTratamientos.animate().rotation(isVisible ? 0f : 180f).setDuration(200).start();
            });
        }

        View layoutAntecedentesMedicos = rootView.findViewById(R.id.layout_antecedentes_medicos);
        final EditText etAntecedentesMedicosView = rootView.findViewById(R.id.et_antecedentes_medicos);
        ImageView flechaAntecedentesMedicos = rootView.findViewById(R.id.flecha_antecedentes_medicos);
        if (layoutAntecedentesMedicos != null && etAntecedentesMedicosView != null && flechaAntecedentesMedicos != null) {
            layoutAntecedentesMedicos.setOnClickListener(v -> {
                boolean isVisible = etAntecedentesMedicosView.getVisibility() == View.VISIBLE;
                etAntecedentesMedicosView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                flechaAntecedentesMedicos.animate().rotation(isVisible ? 0f : 180f).setDuration(200).start();
            });
        }

        View layoutAntecedentesFamiliares = rootView.findViewById(R.id.layout_antecedentes_familiares);
        final EditText etAntecedentesFamiliaresView = rootView.findViewById(R.id.et_antecedentes_familiares);
        ImageView flechaAntecedentesFamiliares = rootView.findViewById(R.id.flecha_antecedentes_familiares);
        if (layoutAntecedentesFamiliares != null && etAntecedentesFamiliaresView != null && flechaAntecedentesFamiliares != null) {
            layoutAntecedentesFamiliares.setOnClickListener(v -> {
                boolean isVisible = etAntecedentesFamiliaresView.getVisibility() == View.VISIBLE;
                etAntecedentesFamiliaresView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                flechaAntecedentesFamiliares.animate().rotation(isVisible ? 0f : 180f).setDuration(200).start();
            });
        }
    }

    private void guardarPerfil() {
        String dpi = etDpi.getText().toString();
        if (!dpi.isEmpty() && dpi.length() != 13) {
            Toast.makeText(getContext(), "El DPI debe tener exactamente 13 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }
        String telefono = etTelefono.getText().toString();
        if (!telefono.isEmpty() && telefono.length() != 8) {
            Toast.makeText(getContext(), "El teléfono debe tener 8 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }
        String emergencia1 = etEmergencia1.getText().toString();
        if (!emergencia1.isEmpty() && emergencia1.length() != 8) {
            Toast.makeText(getContext(), "El teléfono de emergencia 1 debe tener 8 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }
        String emergencia2 = etEmergencia2.getText().toString();
        if (!emergencia2.isEmpty() && emergencia2.length() != 8) {
            Toast.makeText(getContext(), "El teléfono de emergencia 2 debe tener 8 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }
        String emergencia3 = etEmergencia3.getText().toString();
        if (!emergencia3.isEmpty() && emergencia3.length() != 8) {
            Toast.makeText(getContext(), "El teléfono de emergencia 3 debe tener 8 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(PerfilDatabaseHelper.COL_NOMBRES, etNombres.getText().toString());
        values.put(PerfilDatabaseHelper.COL_TELEFONO, etTelefono.getText().toString());
        values.put(PerfilDatabaseHelper.COL_DPI, dpi);
        values.put(PerfilDatabaseHelper.COL_GRUPO_SANGUINEO, spinnerGrupoSanguineo.getSelectedItem().toString());
        values.put(PerfilDatabaseHelper.COL_ALERGIAS, etAlergias.getText().toString());
        values.put(PerfilDatabaseHelper.COL_EMERGENCIA1, etEmergencia1.getText().toString());
        values.put(PerfilDatabaseHelper.COL_EMERGENCIA2, etEmergencia2.getText().toString());
        values.put(PerfilDatabaseHelper.COL_EMERGENCIA3, etEmergencia3.getText().toString());
        values.put(PerfilDatabaseHelper.COL_TRATAMIENTOS, etTratamientos.getText().toString());
        values.put(PerfilDatabaseHelper.COL_ANTECEDENTES_MEDICOS, etAntecedentesMedicos.getText().toString());
        values.put(PerfilDatabaseHelper.COL_ANTECEDENTES_FAMILIARES, etAntecedentesFamiliares.getText().toString());

        dbHelper.guardarPerfil(values);

        Toast.makeText(getContext(), "Perfil guardado exitosamente", Toast.LENGTH_SHORT).show();
        setModoEdicion(false);
    }

    private void cargarDatosGuardados(Cursor cursor) {
        if (cursor == null) return;
        try {
            etNombres.setText(getCursorStringSafe(cursor, PerfilDatabaseHelper.COL_NOMBRES));
            etTelefono.setText(getCursorStringSafe(cursor, PerfilDatabaseHelper.COL_TELEFONO));
            etDpi.setText(getCursorStringSafe(cursor, PerfilDatabaseHelper.COL_DPI));

            String grupoSanguineo = getCursorStringSafe(cursor, PerfilDatabaseHelper.COL_GRUPO_SANGUINEO);
            if (!grupoSanguineo.isEmpty() && spinnerGrupoSanguineo.getAdapter() != null) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerGrupoSanguineo.getAdapter();
                int position = adapter.getPosition(grupoSanguineo);
                if (position >= 0) spinnerGrupoSanguineo.setSelection(position);
            }

            etAlergias.setText(getCursorStringSafe(cursor, PerfilDatabaseHelper.COL_ALERGIAS));
            etEmergencia1.setText(getCursorStringSafe(cursor, PerfilDatabaseHelper.COL_EMERGENCIA1));
            etEmergencia2.setText(getCursorStringSafe(cursor, PerfilDatabaseHelper.COL_EMERGENCIA2));
            etEmergencia3.setText(getCursorStringSafe(cursor, PerfilDatabaseHelper.COL_EMERGENCIA3));
            etTratamientos.setText(getCursorStringSafe(cursor, PerfilDatabaseHelper.COL_TRATAMIENTOS));
            etAntecedentesMedicos.setText(getCursorStringSafe(cursor, PerfilDatabaseHelper.COL_ANTECEDENTES_MEDICOS));
            etAntecedentesFamiliares.setText(getCursorStringSafe(cursor, PerfilDatabaseHelper.COL_ANTECEDENTES_FAMILIARES));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al cargar los datos del perfil", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCursorStringSafe(Cursor cursor, String columnName) {
        if (cursor == null) return "";
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex == -1) return "";
        try {
            return cursor.getString(columnIndex);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Cursor cursor = dbHelper.obtenerPerfil();
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
        } else {
            setModoEdicion(true); 
        }
    }

    private void setModoEdicion(boolean editable) {
        modoEdicion = editable;
        EditText[] editTexts = {etNombres, etTelefono, etDpi, etAlergias,
                etEmergencia1, etEmergencia2, etEmergencia3,
                etTratamientos, etAntecedentesMedicos, etAntecedentesFamiliares};

        for (EditText et : editTexts) {
            if (et != null) et.setEnabled(editable);
        }

        View[] layouts = {
            rootView.findViewById(R.id.layout_tratamientos),
            rootView.findViewById(R.id.layout_antecedentes_medicos),
            rootView.findViewById(R.id.layout_antecedentes_familiares)
        };
        for (View layout : layouts) {
            if (layout != null) {
                layout.setEnabled(true);
                layout.setClickable(true);
            }
        }

        if (spinnerGrupoSanguineo != null) spinnerGrupoSanguineo.setEnabled(editable);
        if (btnGuardarPerfil != null) btnGuardarPerfil.setVisibility(editable ? View.VISIBLE : View.GONE);
        if (btnEditar != null) btnEditar.setVisibility(editable ? View.GONE : View.VISIBLE);
    }

    private void habilitarEdicion() {
        setModoEdicion(true);
    }

    private void configurarGoogleDrive() {
        btnGoogleDriveAction.setOnClickListener(v -> {
            if (authManager.isSignedIn()) {
                // Usuario está conectado, mostrar opción de desconectar
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Desconectar Google Drive")
                    .setMessage("¿Deseas desconectar tu cuenta de Google? Las notas seguirán guardándose localmente.")
                    .setPositiveButton("Desconectar", (dialog, which) -> {
                        authManager.signOut();
                        actualizarEstadoGoogleDrive();
                        Toast.makeText(requireContext(), "Desconectado de Google Drive", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            } else {
                // Usuario no está conectado, iniciar login
                authManager.startSignIn(requireActivity(), RC_GOOGLE_SIGN_IN);
            }
        });
    }

    private void actualizarEstadoGoogleDrive() {
        if (authManager.isSignedIn()) {
            tvGoogleDriveStatus.setText("✓ Conectado - Tus notas se respaldan automáticamente");
            tvGoogleDriveStatus.setTextColor(getResources().getColor(R.color.azul_principal));
            btnGoogleDriveAction.setText("Desconectar");
            btnGoogleDriveAction.setBackgroundTintList(
                getResources().getColorStateList(R.color.rojo_emergencia)
            );
        } else {
            tvGoogleDriveStatus.setText("⚠ No conectado - Las notas solo se guardan localmente");
            tvGoogleDriveStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
            btnGoogleDriveAction.setText("Conectar Google Drive");
            btnGoogleDriveAction.setBackgroundTintList(
                getResources().getColorStateList(R.color.verde_claro)
            );
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN && data != null) {
            authManager.handleAuthorizationResponse(data, new GoogleAuthManager.AuthCallback() {
                @Override
                public void onSuccess() {
                    requireActivity().runOnUiThread(() -> {
                        actualizarEstadoGoogleDrive();
                        Toast.makeText(requireContext(),
                            "✓ Conectado a Google Drive correctamente",
                            Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(),
                            "Error al conectar: " + error,
                            Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
    }
}
