package com.example.lista_medica2dointento;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CrearNotaFragment extends Fragment {
    private NotasDatabaseHelper dbHelper;
    private TabLayout tabLayout;
    private ViewGroup contenedorFormulario;
    private View formularioActual;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_crear_nota, container, false);
        dbHelper = new NotasDatabaseHelper(getContext());
        tabLayout = rootView.findViewById(R.id.tab_crear_nota);
        contenedorFormulario = rootView.findViewById(R.id.contenedor_crear_nota);

        tabLayout.addTab(tabLayout.newTab().setText("Nota Médica"));
        tabLayout.addTab(tabLayout.newTab().setText("Receta"));
        tabLayout.addTab(tabLayout.newTab().setText("Nota"));

        mostrarFormulario(0);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mostrarFormulario(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        return rootView;
    }
    private void mostrarFormulario(int tipo) {
        contenedorFormulario.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        switch (tipo) {
            case 0: // Nota Médica
                formularioActual = inflater.inflate(R.layout.formulario_nota_medica, contenedorFormulario, false);
                Button btnGuardarMedica = formularioActual.findViewById(R.id.btn_guardar_nota_medica);
                btnGuardarMedica.setOnClickListener(v -> guardarNotaMedica());
                configurarSelectorFecha(formularioActual.findViewById(R.id.et_fecha_nota_medica));
                break;
            case 1: // Receta
                formularioActual = inflater.inflate(R.layout.formulario_receta, contenedorFormulario, false);
                Button btnGuardarReceta = formularioActual.findViewById(R.id.btn_guardar_receta);
                btnGuardarReceta.setOnClickListener(v -> guardarReceta());
                configurarSelectorFecha(formularioActual.findViewById(R.id.et_fecha_receta));
                break;
            case 2: // Nota
                formularioActual = inflater.inflate(R.layout.formulario_nota, contenedorFormulario, false);
                Button btnGuardarNota = formularioActual.findViewById(R.id.btn_guardar_nota);
                btnGuardarNota.setOnClickListener(v -> guardarNotaGeneral());
                configurarSelectorFecha(formularioActual.findViewById(R.id.et_fecha_nota));
                break;
        }
        contenedorFormulario.addView(formularioActual);

    }

    private void configurarSelectorFecha(EditText etFecha) {
        if (etFecha == null) return;
        etFecha.setFocusable(false);
        etFecha.setClickable(true);
        etFecha.setOnClickListener(v -> mostrarDatePicker(etFecha));
        etFecha.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) mostrarDatePicker(etFecha);
        });
    }

    private void mostrarDatePicker(EditText etFecha) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                etFecha.setText(sdf.format(calendar.getTime()));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void guardarNotaMedica() {
        EditText etFecha = formularioActual.findViewById(R.id.et_fecha_nota_medica);
        EditText etAsunto = formularioActual.findViewById(R.id.et_asunto_nota_medica);
        EditText etContenido = formularioActual.findViewById(R.id.et_contenido_nota_medica);

        String fecha = etFecha.getText().toString().trim();
        String asunto = etAsunto.getText().toString().trim();
        String contenido = etContenido.getText().toString().trim();

        // VALIDACIÓN: ningún campo puede estar vacío
        String faltantes = construirCamposFaltantes(
                new String[][]{{"Fecha", fecha}, {"Asunto", asunto}, {"Contenido", contenido}}
        );
        if (!faltantes.isEmpty()) {
            Toast.makeText(getContext(), "Complete los campos faltantes", Toast.LENGTH_LONG).show();
            EditText first = obtenerPrimerEditTextVacio(
                    new String[][]{{"Fecha", fecha}, {"Asunto", asunto}, {"Contenido", contenido}},
                    new EditText[]{etFecha, etAsunto, etContenido}
            );
            if (first != null) first.requestFocus();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("fecha", fecha);
        values.put("asunto", asunto);
        values.put("contenido", contenido);

        long id = dbHelper.getWritableDatabase().insert(NotasDatabaseHelper.TABLA_NOTAS_MEDICAS, null, values);

        if (id != -1) {
            Toast.makeText(getContext(), "Nota médica guardada", Toast.LENGTH_SHORT).show();

            // Crear objeto NotaItem y generar/subir PDF automáticamente
            NotaItem nota = new NotaItem((int)id, "Nota Médica", asunto, fecha, contenido, System.currentTimeMillis());
            generarYSubirPDF(nota, contenido);

            limpiarCampos(formularioActual);
        } else {
            Toast.makeText(getContext(), "Error al guardar la nota", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarReceta() {
        EditText etFecha = formularioActual.findViewById(R.id.et_fecha_receta);
        EditText etRecetadoPor = formularioActual.findViewById(R.id.et_recetado_por);
        EditText etMedicamentos = formularioActual.findViewById(R.id.et_medicamentos);
        EditText etIndicaciones = formularioActual.findViewById(R.id.et_indicaciones_receta);

        String fecha = etFecha.getText().toString().trim();
        String recetadoPor = etRecetadoPor.getText().toString().trim();
        String medicamentos = etMedicamentos.getText().toString().trim();
        String indicaciones = etIndicaciones.getText().toString().trim();

        String faltantes = construirCamposFaltantes(
                new String[][]{{"Fecha", fecha}, {"Recetado por", recetadoPor}, {"Medicamentos", medicamentos}, {"Indicaciones", indicaciones}}
        );
        if (!faltantes.isEmpty()) {
            Toast.makeText(getContext(), "Complete los campos faltantes", Toast.LENGTH_LONG).show();
            EditText first = obtenerPrimerEditTextVacio(
                    new String[][]{{"Fecha", fecha}, {"Recetado por", recetadoPor}, {"Medicamentos", medicamentos}, {"Indicaciones", indicaciones}},
                    new EditText[]{etFecha, etRecetadoPor, etMedicamentos, etIndicaciones}
            );
            if (first != null) first.requestFocus();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("fecha", fecha);
        values.put("recetado_por", recetadoPor);
        values.put("medicamentos", medicamentos);
        values.put("indicaciones", indicaciones);

        long id = dbHelper.getWritableDatabase().insert(NotasDatabaseHelper.TABLA_RECETAS, null, values);

        if (id != -1) {
            Toast.makeText(getContext(), "Receta guardada", Toast.LENGTH_SHORT).show();


            String contenidoCombinado = "Medicamentos: " + medicamentos + "\nIndicaciones: " + indicaciones;
            NotaItem nota = new NotaItem((int)id, "Receta", recetadoPor, fecha, contenidoCombinado, System.currentTimeMillis());
            generarYSubirPDF(nota, "Medicamentos: " + medicamentos + "\n" + (indicaciones.isEmpty() ? "" : "Indicaciones: " + indicaciones));

            limpiarCampos(formularioActual);
        } else {
            Toast.makeText(getContext(), "Error al guardar la receta", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarNotaGeneral() {
        EditText etFecha = formularioActual.findViewById(R.id.et_fecha_nota);
        EditText etAsunto = formularioActual.findViewById(R.id.et_asunto_nota);
        EditText etContenido = formularioActual.findViewById(R.id.et_contenido_nota);

        String fecha = etFecha.getText().toString().trim();
        String asunto = etAsunto.getText().toString().trim();
        String contenido = etContenido.getText().toString().trim();


        String faltantes = construirCamposFaltantes(
                new String[][]{{"Fecha", fecha}, {"Asunto", asunto}, {"Contenido", contenido}}
        );
        if (!faltantes.isEmpty()) {
            Toast.makeText(getContext(), "Complete los campos faltantes", Toast.LENGTH_LONG).show();
            EditText first = obtenerPrimerEditTextVacio(
                    new String[][]{{"Fecha", fecha}, {"Asunto", asunto}, {"Contenido", contenido}},
                    new EditText[]{etFecha, etAsunto, etContenido}
            );
            if (first != null) first.requestFocus();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("fecha", fecha);
        values.put("asunto", asunto);
        values.put("contenido", contenido);

        long id = dbHelper.getWritableDatabase().insert(NotasDatabaseHelper.TABLA_NOTAS, null, values);

        if (id != -1) {
            Toast.makeText(getContext(), "Nota guardada", Toast.LENGTH_SHORT).show();


            NotaItem nota = new NotaItem((int)id, "Nota", asunto, fecha, contenido, System.currentTimeMillis());
            generarYSubirPDF(nota, contenido);

            limpiarCampos(formularioActual);
        } else {
            Toast.makeText(getContext(), "Error al guardar la nota", Toast.LENGTH_SHORT).show();
        }
    }

    private String construirCamposFaltantes(String[][] campos) {
        if (campos == null || campos.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String[] par : campos) {
            if (par == null || par.length < 2) continue;
            String nombre = par[0];
            String valor = par[1];
            if (valor == null || valor.trim().isEmpty()) {
                if (!first) sb.append(", ");
                sb.append(nombre);
                first = false;
            }
        }
        return sb.toString();
    }

    private void limpiarCampos(View formulario) {
        if (formulario == null) return;
        for (int i = 0; i < ((ViewGroup)formulario).getChildCount(); i++) {
            View v = ((ViewGroup)formulario).getChildAt(i);
            if (v instanceof EditText) {
                ((EditText) v).setText("");
            } else if (v instanceof ViewGroup) {
                limpiarCampos(v);
            }
        }
    }

    private void generarYSubirPDF(NotaItem nota, String contenido) {
        android.util.Log.d("CrearNotaFragment", "=== GENERANDO Y SUBIENDO PDF ===");
        android.util.Log.d("CrearNotaFragment", "Tipo: " + nota.tipo + ", Asunto: " + nota.asunto);

        try {
            File pdfFile = crearPDFNota(nota, contenido);

            if (pdfFile != null) {
                android.util.Log.d("CrearNotaFragment", "PDF creado: " + pdfFile.getAbsolutePath());
                subirPDFaN8n(pdfFile, nota);
            } else {
                android.util.Log.e("CrearNotaFragment", "Error: PDF es NULL");
            }
        } catch (Exception e) {
            android.util.Log.e("CrearNotaFragment", "Error generando/subiendo PDF", e);
            e.printStackTrace();
        }
    }

    private void subirPDFaN8n(File pdfFile, NotaItem nota) {
        GoogleAuthManager authManager = new GoogleAuthManager(requireContext());
        String accessToken = null;

        if (authManager.isSignedIn()) {
            accessToken = authManager.getAccessToken();
            android.util.Log.d("CrearNotaFragment", "Usuario autenticado con Google");
        } else {
            android.util.Log.d("CrearNotaFragment", "Usuario NO autenticado, subiendo sin token");
        }

        realizarSubida(pdfFile, nota, accessToken);
    }

    private void realizarSubida(File pdfFile, NotaItem nota, String accessToken) {
        N8nUploader uploader = new N8nUploader();

        Toast.makeText(requireContext(), "📤 Subiendo nota a la nube...", Toast.LENGTH_SHORT).show();

        uploader.uploadPdf(
            pdfFile,
            nota.tipo,
            nota.asunto != null ? nota.asunto : "Sin asunto",
            null,
            accessToken,
            new N8nUploader.UploadCallback() {
                @Override
                public void onSuccess(String response) {
                    requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "✓ Nota respaldada en la nube", Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onError(String error) {
                    android.util.Log.e("CrearNotaFragment", "Error subiendo a n8n: " + error);
                    requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "⚠ Nota guardada localmente", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        );
    }

    private File crearPDFNota(NotaItem nota, String contenido) throws IOException {
        PdfDocument document = new PdfDocument();
        int pageWidth = 595, pageHeight = 842;
        int margin = 40;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        int x = margin, y = margin + 20;
        int maxWidth = pageWidth - 2 * margin;

        String tipoMinus = nota.tipo != null ? nota.tipo.toLowerCase() : "";
        String tituloHeader;
        if (tipoMinus.contains("receta")) tituloHeader = "RECETA MÉDICA";
        else if (tipoMinus.contains("médica") || tipoMinus.contains("medica")) tituloHeader = "NOTA MÉDICA";
        else tituloHeader = "NOTA";

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF1976D2);
        canvas.drawRect(0, 0, pageWidth, margin - 8, paint);

        paint.setColor(0xFFFFFFFF);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        canvas.drawText(tituloHeader, pageWidth / 2f, margin - 16, paint);

        paint.setColor(0xFF1976D2);
        paint.setStrokeWidth(2);
        canvas.drawLine(x, y - 6, pageWidth - x, y - 6, paint);

        paint.setColor(0xFF000000);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(12);
        paint.setFakeBoldText(true);
        canvas.drawText("Fecha: " + (nota.fecha != null ? nota.fecha : ""), x, y + 4, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        String rightMeta = (nota.asunto != null && !nota.asunto.isEmpty()) ? nota.asunto : "";
        canvas.drawText(rightMeta, pageInfo.getPageWidth() - x, y + 4, paint);

        y += 28;


        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(11);
        paint.setFakeBoldText(false);

        String[] lineas = contenido.split("\n");
        for (String linea : lineas) {
            if (y + 20 > pageHeight - margin) break;
            canvas.drawText(linea, x, y, paint);
            y += 16;
        }

        // Pie de página con barra azul
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF1976D2);
        canvas.drawRect(0, pageHeight - margin + 8, pageWidth, pageHeight, paint);

        document.finishPage(page);

        File cacheDir = requireContext().getCacheDir();
        String fileName = nota.tipo.replaceAll("\\s+", "_").toLowerCase() + "_" + System.currentTimeMillis() + ".pdf";
        File pdfFile = new File(cacheDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            document.writeTo(fos);
        }
        document.close();

        return pdfFile;
    }

    private EditText obtenerPrimerEditTextVacio(String[][] campos, EditText[] edits) {
        if (campos == null || edits == null) return null;
        int len = Math.min(campos.length, edits.length);
        for (int i = 0; i < len; i++) {
            String[] par = campos[i];
            if (par == null || par.length < 2) continue;
            String valor = par[1];
            if (valor == null || valor.trim().isEmpty()) {
                return edits[i];
            }
        }
        return null;
    }
}
