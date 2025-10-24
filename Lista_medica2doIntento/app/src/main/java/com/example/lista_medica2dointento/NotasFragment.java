package com.example.lista_medica2dointento;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.snackbar.Snackbar;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotasFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotasAdapter adapter;
    private NotasDatabaseHelper dbHelper;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private View rootView;
    private GoogleAuthManager authManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_notas, container, false);

        recyclerView = rootView.findViewById(R.id.rv_notas);
        if (recyclerView == null) {
            Toast.makeText(getContext(), "Error: RecyclerView no encontrado en el layout.", Toast.LENGTH_LONG).show();
            return rootView;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new NotasDatabaseHelper(getContext());
        authManager = new GoogleAuthManager(requireContext()); // Crear UNA SOLA VEZ
        cargarNotas();
        return rootView;
    }

    private void cargarNotas() {
        List<NotaItem> lista = new ArrayList<>();
        // Notas médicas
        Cursor c1 = dbHelper.getReadableDatabase().rawQuery(
                "SELECT id, fecha, asunto, contenido FROM notas_medicas ORDER BY id DESC", null);
        if (c1 != null) {
            while (c1.moveToNext()) {
                int id = c1.getInt(0);
                String fecha = c1.getString(1);
                String asunto = c1.getString(2);
                String contenido = c1.getString(3);
                long ts = parseFecha(fecha);
                lista.add(new NotaItem(id, "Nota Médica", asunto, fecha, contenido, ts));
            }
            c1.close();
        }
        // Recetas
        Cursor c2 = dbHelper.getReadableDatabase().rawQuery(
                "SELECT id, fecha, recetado_por, medicamentos, indicaciones FROM recetas ORDER BY id DESC", null);
        if (c2 != null) {
            while (c2.moveToNext()) {
                int id = c2.getInt(0);
                String fecha = c2.getString(1);
                String recetadoPor = c2.getString(2);
                String asunto = "Recetado por: " + recetadoPor;
                String contenido = "Medicamentos: " + c2.getString(3) + "\nIndicaciones: " + c2.getString(4);
                long ts = parseFecha(fecha);
                lista.add(new NotaItem(id, "Receta", asunto, fecha, contenido, ts));
            }
            c2.close();
        }
        // Notas generales
        Cursor c3 = dbHelper.getReadableDatabase().rawQuery(
                "SELECT id, fecha, asunto, contenido FROM notas_generales ORDER BY id DESC", null);
        if (c3 != null) {
            while (c3.moveToNext()) {
                int id = c3.getInt(0);
                String fecha = c3.getString(1);
                String asunto = c3.getString(2);
                String contenido = c3.getString(3);
                long ts = parseFecha(fecha);
                lista.add(new NotaItem(id, "Nota", asunto, fecha, contenido, ts));
            }
            c3.close();
        }
        Collections.sort(lista, (o1, o2) -> Long.compare(o2.timestamp, o1.timestamp));
        adapter = new NotasAdapter(lista);
        adapter.setOnNotaEliminarListener(this::mostrarDialogoEliminar);
        adapter.setOnNotaCompartirListener(this::compartirNotaComoPDF);
        recyclerView.setAdapter(adapter);
    }

    private void mostrarDialogoEliminar(NotaItem nota, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar esta " + nota.tipo + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarNota(nota, position))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarNota(NotaItem nota, int position) {
        try {
            String table;
            String where = "id=?";
            String[] args = new String[]{String.valueOf(nota.id)};
            if (nota.tipo.equals("Nota Médica")) {
                table = NotasDatabaseHelper.TABLA_NOTAS_MEDICAS;
            } else if (nota.tipo.equals("Receta")) {
                table = NotasDatabaseHelper.TABLA_RECETAS;
            } else {
                table = NotasDatabaseHelper.TABLA_NOTAS;
            }
            int rowsDeleted = dbHelper.getWritableDatabase().delete(table, where, args);
            if (rowsDeleted > 0) {
                adapter.removeNota(position);
                Snackbar.make(rootView, nota.tipo + " eliminada", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(rootView, "No se pudo eliminar la nota", Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Snackbar.make(rootView, "Error al eliminar: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void compartirNotaComoPDF(NotaItem nota) {
        android.util.Log.d("NotasFragment", ">>> compartirNotaComoPDF llamado para: " + nota.tipo + " - " + nota.asunto);
        Toast.makeText(requireContext(), "🔄 Generando y subiendo PDF...", Toast.LENGTH_SHORT).show();

        // Construir sólo el contenido principal para el recuadro del PDF (sin repetir tipo/asunto/fecha)
        String bodyContent;
        if (nota.tipo.equals("Nota Médica")) {
            bodyContent = nota.contenido != null ? nota.contenido : "";
        } else if (nota.tipo.equals("Receta")) {
            // nota.contenido fue guardada como "Medicamentos: ...\nIndicaciones: ..."
            String[] lineas = nota.contenido != null ? nota.contenido.split("\\n") : new String[0];
            String medicamentos = lineas.length > 0 ? lineas[0].replaceFirst("(?i)Medicamentos:\\s*", "") : "";
            String indicaciones = lineas.length > 1 ? lineas[1].replaceFirst("(?i)Indicaciones:\\s*", "") : "";
            bodyContent = "Medicamentos: " + medicamentos + "\n" + (indicaciones.isEmpty() ? "" : "Indicaciones: " + indicaciones);
        } else {
            bodyContent = nota.contenido != null ? nota.contenido : "";
        }
        try {
            android.util.Log.d("NotasFragment", ">>> Creando PDF...");
            File pdfFile = crearPDFNota(nota, bodyContent);
            android.util.Log.d("NotasFragment", ">>> PDF creado: " + (pdfFile != null ? pdfFile.getAbsolutePath() : "NULL"));

             if (pdfFile != null) {
                // Subir automáticamente a Google Drive vía n8n
                android.util.Log.d("NotasFragment", ">>> Llamando a subirPDFaN8n...");
                subirPDFaN8n(pdfFile, nota);

                // También permitir compartir manualmente
                Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", pdfFile);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Compartir nota como PDF"));
            } else {
                android.util.Log.e("NotasFragment", ">>> ERROR: PDF es NULL");
                Toast.makeText(requireContext(), "Error: no se pudo crear el PDF", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            android.util.Log.e("NotasFragment", ">>> EXCEPCIÓN al compartir PDF", e);
            e.printStackTrace();
            Snackbar.make(rootView, "Error al generar PDF: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void subirPDFaN8n(File pdfFile, NotaItem nota) {
        android.util.Log.d("NotasFragment", "=== INICIANDO SUBIDA A N8N ===");
        android.util.Log.d("NotasFragment", "Archivo PDF: " + pdfFile.getAbsolutePath());
        android.util.Log.d("NotasFragment", "Tamaño: " + pdfFile.length() + " bytes");
        android.util.Log.d("NotasFragment", "Tipo nota: " + nota.tipo);
        android.util.Log.d("NotasFragment", "Asunto: " + nota.asunto);

        String accessToken = null;

        if (authManager.isSignedIn()) {
            accessToken = authManager.getAccessToken();
            if (accessToken != null && !accessToken.isEmpty()) {
                android.util.Log.d("NotasFragment", "Usuario autenticado, token obtenido: " + accessToken.substring(0, Math.min(20, accessToken.length())) + "...");
            } else {
                android.util.Log.w("NotasFragment", "Usuario autenticado pero token es null o vacío");
            }
        } else {
            android.util.Log.d("NotasFragment", "Usuario NO autenticado, subiendo sin token");
            // Mostrar diálogo para próxima vez (no bloquea la subida)
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(),
                    "💡 Inicia sesión con Google para guardar automáticamente en Drive",
                    Toast.LENGTH_LONG).show();
            });
        }

        realizarSubida(pdfFile, nota, accessToken, authManager);
    }

    private void realizarSubida(File pdfFile, NotaItem nota, String accessToken, GoogleAuthManager authManager) {
        N8nUploader uploader = new N8nUploader();

        // Obtener email del usuario si está autenticado
        String userEmail = null;
        android.util.Log.d("NotasFragment", "=== VERIFICANDO EMAIL DEL USUARIO ===");
        android.util.Log.d("NotasFragment", "isSignedIn: " + authManager.isSignedIn());

        // Método 1: Intentar obtener email desde la cuenta de Google Sign-In
        if (authManager.isSignedIn()) {
            com.google.android.gms.auth.api.signin.GoogleSignInAccount account = authManager.getSignedInAccount();
            android.util.Log.d("NotasFragment", "Account obtenido: " + (account != null ? "SÍ" : "NULL"));

            if (account != null) {
                userEmail = account.getEmail();
                android.util.Log.d("NotasFragment", "✅ Email desde account: " + userEmail);
            } else {
                android.util.Log.w("NotasFragment", "⚠️ Account es NULL, intentando extraer del token...");
            }
        }

        // Método 2: Si no se pudo obtener el email de la cuenta, extraerlo del token JWT
        if (userEmail == null && accessToken != null && !accessToken.isEmpty()) {
            android.util.Log.d("NotasFragment", "Intentando extraer email del token JWT...");
            userEmail = authManager.getEmailFromToken(accessToken);
            if (userEmail != null) {
                android.util.Log.d("NotasFragment", "✅ Email extraído del token: " + userEmail);
            } else {
                android.util.Log.e("NotasFragment", "❌ No se pudo extraer email del token");
            }
        }

        if (userEmail == null) {
            android.util.Log.w("NotasFragment", "❌ No se pudo obtener email por ningún método");
        }

        uploader.uploadPdf(
            pdfFile,
            nota.tipo,
            nota.asunto != null ? nota.asunto : "Sin asunto",
            userEmail,
            accessToken,
            new N8nUploader.UploadCallback() {
                @Override
                public void onSuccess(String response) {
                    requireActivity().runOnUiThread(() ->
                        Snackbar.make(rootView, "✓ Nota guardada en Google Drive", Snackbar.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() ->
                        Snackbar.make(rootView, "Error subiendo a Drive: " + error, Snackbar.LENGTH_LONG).show()
                    );
                }
            }
        );
    }

    private File crearPDFNota(NotaItem nota, String contenido) throws IOException {
        PdfDocument document = new PdfDocument();
        int pageWidth = 595, pageHeight = 842; // A4 aproximado en puntos
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

        // Cabecera superior con barra azul
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF1976D2); // Azul
        canvas.drawRect(0, 0, pageWidth, margin - 8, paint);

        // Título principal centrado (blanco sobre barra)
        paint.setColor(0xFFFFFFFF);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        canvas.drawText(tituloHeader, pageWidth / 2f, margin - 16, paint);

        // Separador
        paint.setColor(0xFF1976D2);
        paint.setStrokeWidth(2);
        canvas.drawLine(x, y - 6, pageWidth - x, y - 6, paint);

        // Cabecera de metadatos: Fecha a la izquierda, asunto / recetado por a la derecha
        paint.setColor(0xFF000000);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(12);
        paint.setFakeBoldText(true);
        canvas.drawText("Fecha: " + (nota.fecha != null ? nota.fecha : ""), x, y + 4, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        String rightMeta = "";
        if (tipoMinus.contains("receta")) {
            // intentar extraer recetado por
            if (nota.asunto != null && nota.asunto.toLowerCase().startsWith("recetado por:")) {
                rightMeta = nota.asunto.substring("recetado por:".length()).trim();
            } else {
                // buscar en contenido
                String[] lines = contenido.split("\\n");
                for (String l : lines) {
                    if (l.toLowerCase().startsWith("recetado por")) {
                        int idx = l.indexOf(":");
                        if (idx != -1 && idx + 1 < l.length()) {
                            rightMeta = l.substring(idx + 1).trim();
                            break;
                        }
                    }
                }
            }
            if (!rightMeta.isEmpty()) rightMeta = "Prescrito por: " + rightMeta;
            else rightMeta = "Prescrito por: --";
        } else {
            // mostrar asunto si existe
            rightMeta = (nota.asunto != null && !nota.asunto.isEmpty()) ? nota.asunto : "";
        }
        canvas.drawText(rightMeta, pageInfo.getPageWidth() - x, y + 4, paint);

        y += 28;

        // Usar directamente el contenido pasado al método (parámetro) para asegurar que el PDF muestre lo que se construyó al compartir
        String mainContent = contenido != null ? contenido.trim() : "";

        // Caja azul principal: contiene la información ingresada por el usuario (mainContent)
        float boxTop = y;
        int boxHeightEstimate = 120; // inicial
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFF3F9FF); // Fondo muy suave azul
        canvas.drawRoundRect(new android.graphics.RectF(x, y, pageWidth - x, y + boxHeightEstimate), 8f, 8f, paint);

        // Título de la sección dentro del recuadro
        paint.setColor(0xFF1976D2);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        canvas.drawText("Información:", x + 10, y + 22, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(12);
        paint.setColor(0xFF000000);
        int writeY = y + 42;
        // escribir mainContent respetando saltos de línea
        if (!mainContent.isEmpty()) {
            writeY = drawMultilineText(canvas, paint, mainContent, x + 10, writeY, maxWidth - 20, 18);
        } else {
            // si no hay contenido, dejar un placeholder
            writeY = drawMultilineText(canvas, paint, "(Sin información ingresada)", x + 10, writeY, maxWidth - 20, 18);
        }

        // Si el contenido supera la caja, redibujar la caja para ajustarla verticalmente
        int usedBoxBottom = writeY + 8;
        if (usedBoxBottom > y + boxHeightEstimate) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFF3F9FF);
            canvas.drawRoundRect(new android.graphics.RectF(x, boxTop, pageWidth - x, usedBoxBottom), 8f, 8f, paint);
        }

        y = usedBoxBottom + 8;

        // Eliminada la caja de indicaciones separada: todas las indicaciones (si las hay) ya están en mainContent

        // Pie azul inferior
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF1976D2);
        canvas.drawRect(x, pageHeight - margin, pageWidth - x, pageHeight - margin + 8, paint);

        // Nota informativa en pie (solo si no es receta)
        if (!tipoMinus.contains("receta")) {
            paint.setColor(0xFF000000);
            paint.setTextSize(12);
            paint.setFakeBoldText(false);
            canvas.drawText("Nota Generada en Listado de Notas", x, pageHeight - margin - 20, paint);
        }

        document.finishPage(page);

        File pdfDir = new File(requireContext().getCacheDir(), "pdfs");
        if (!pdfDir.exists()) pdfDir.mkdirs();
        File pdfFile = new File(pdfDir, "nota_" + System.currentTimeMillis() + ".pdf");
        FileOutputStream fos = new FileOutputStream(pdfFile);
        document.writeTo(fos);
        document.close();
        fos.close();
        return pdfFile;
    }

    private int drawMultilineText(Canvas canvas, Paint paint, String text, int x, int y, int maxWidth, int lineHeight) {
        if (text == null) return y;
        String[] lines = text.split("\\n");
        for (String line : lines) {
            // Manejar líneas vacías para preservar saltos de línea
            if (line.isEmpty()) {
                y += lineHeight;
                continue;
            }
            while (line.length() > 0) {
                int count = paint.breakText(line, true, maxWidth, null);
                canvas.drawText(line.substring(0, count), x, y, paint);
                line = line.substring(count);
                y += lineHeight;
            }
        }
        return y;
    }

    private String getContenidoPrincipal(NotaItem nota) {
        if (nota.tipo.equals("Nota Médica")) {
            // Ahora simplemente devolvemos el contenido (descripción/detalles)
            return nota.contenido != null ? nota.contenido : "";
        } else if (nota.tipo.equals("Receta")) {
            String[] lineas = nota.contenido.split("\\n");
            return (lineas.length > 0 ? lineas[0] : ""); 
        } else {
            return nota.contenido;
        }
    }

    private String getOtrosContenido(NotaItem nota) {
        if (nota.tipo.equals("Nota Médica")) {
            // Ya no hay contenido adicional para notas médicas
            return "";
        } else if (nota.tipo.equals("Receta")) {
            String[] lineas = nota.contenido.split("\\n");
            StringBuilder sb = new StringBuilder();
            if (lineas.length > 1) { 
                String indicaciones = lineas[1];
                if (indicaciones.toLowerCase().startsWith("indicaciones:")) {
                    sb.append(indicaciones).append("\n");
                } else {
                    sb.append("Indicaciones:\n").append(indicaciones).append("\n");
                }
            }
            return sb.toString();
        }
        return "";
    }

    private long parseFecha(String fecha) {
        try {
            Date d = sdf.parse(fecha);
            return d != null ? d.getTime() : 0;
        } catch (ParseException e) {
            return 0;
        }
    }
}
