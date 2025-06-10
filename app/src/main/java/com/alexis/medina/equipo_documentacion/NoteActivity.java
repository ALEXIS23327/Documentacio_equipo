package com.alexis.medina.equipo_documentacion;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteActivity extends AppCompatActivity {

    TextView dateView;
    EditText noteEditText;
    Button saveButton, btnBold, btnItalic, btnUnderline, btnBullet, btnFavorito, exportButton, btnDeleteNote;
    Spinner categorySpinner;
    String selectedDate, noteId, today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        Button btnChecklist = findViewById(R.id.btnChecklist);
        btnChecklist.setOnClickListener(v -> {
            int pos = noteEditText.getSelectionStart();
            noteEditText.getText().insert(pos, "☐ ");
        });

        // Inicialización
        dateView = findViewById(R.id.dateView);
        noteEditText = findViewById(R.id.noteEditText);
        saveButton = findViewById(R.id.saveButton);
        btnBold = findViewById(R.id.btnBold);
        btnItalic = findViewById(R.id.btnItalic);
        btnUnderline = findViewById(R.id.btnUnderline);
        btnBullet = findViewById(R.id.btnBullet);
        btnFavorito = findViewById(R.id.btnFavorito);
        exportButton = findViewById(R.id.exportButton);
        btnDeleteNote = findViewById(R.id.btnDeleteNote);
        categorySpinner = findViewById(R.id.categorySpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categorias_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        selectedDate = getIntent().getStringExtra("selectedDate");
        noteId = getIntent().getStringExtra("noteId");
        dateView.setText("Fecha: " + selectedDate);

        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        noteEditText.setText(loadFormattedNote(noteId));
        loadCategoriaSeleccionada(noteId);

        boolean isToday = selectedDate.equals(today);
        boolean isFuture = selectedDate.compareTo(today) > 0;
        boolean isPast = selectedDate.compareTo(today) < 0;

        if (isToday || isFuture) {
            noteEditText.setEnabled(true);
            saveButton.setEnabled(true);
            categorySpinner.setEnabled(true);
            if (isFuture) noteEditText.setHint("Pendiente: ...");
        } else {
            noteEditText.setEnabled(false);
            saveButton.setEnabled(false);
            categorySpinner.setEnabled(false);
            disableToolbar();
        }

        saveButton.setOnClickListener(v -> {
            saveNote(noteId, noteEditText.getText());
            saveCategoriaSeleccionada(noteId);
            Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show();
        });

        exportButton.setOnClickListener(v -> showExportDialog());

        noteEditText.addTextChangedListener(new TextWatcher() {
            boolean editing = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (editing) return;
                editing = true;

                String fullText = s.toString();
                int index = fullText.indexOf("\n");
                SpannableStringBuilder builder = new SpannableStringBuilder(fullText);
                builder.clearSpans();

                if (index != -1) {
                    builder.setSpan(new StyleSpan(Typeface.BOLD), 0, index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(new RelativeSizeSpan(1.5f), 0, index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    builder.setSpan(new StyleSpan(Typeface.BOLD), 0, fullText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(new RelativeSizeSpan(1.5f), 0, fullText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                noteEditText.removeTextChangedListener(this);
                noteEditText.setText(builder);
                noteEditText.setSelection(builder.length());
                noteEditText.addTextChangedListener(this);
                editing = false;
            }
        });

        btnBold.setOnClickListener(v -> applyStyle(new StyleSpan(Typeface.BOLD)));
        btnItalic.setOnClickListener(v -> applyStyle(new StyleSpan(Typeface.ITALIC)));
        btnUnderline.setOnClickListener(v -> applyStyle(new UnderlineSpan()));
        btnBullet.setOnClickListener(v -> {
            int start = noteEditText.getSelectionStart();
            noteEditText.getText().insert(start, "• ");
        });

        btnFavorito.setOnClickListener(v -> {
            if (FavoritosManager.esFavorito(this, noteId)) {
                FavoritosManager.eliminarFavorito(this, noteId);
                btnFavorito.setText("☆");
                Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
            } else {
                FavoritosManager.guardarFavorito(this, noteId);
                btnFavorito.setText("★");
                Toast.makeText(this, "Añadido a favoritos", Toast.LENGTH_SHORT).show();
            }
        });

        btnFavorito.setText(FavoritosManager.esFavorito(this, noteId) ? "★" : "☆");

        // IMPLEMENTACIÓN DE CHECKLIST (☐ ↔ ☑)
        noteEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int offset = noteEditText.getOffsetForPosition(event.getX(), event.getY());
                Editable text = noteEditText.getText();

                int lineStart = text.toString().lastIndexOf('\n', offset - 1) + 1;
                int lineEnd = text.toString().indexOf('\n', offset);
                if (lineEnd == -1) lineEnd = text.length();

                String line = text.subSequence(lineStart, lineEnd).toString();

                if (line.trim().startsWith("☐")) {
                    String newLine = line.replaceFirst("☐", "☑");
                    text.replace(lineStart, lineEnd, newLine);
                } else if (line.trim().startsWith("☑")) {
                    String newLine = line.replaceFirst("☑", "☐");
                    text.replace(lineStart, lineEnd, newLine);
                }
            }
            return false;
        });

        btnDeleteNote.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar nota")
                    .setMessage("¿Estás seguro de que quieres eliminar esta nota?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        eliminarNota(noteId);
                        Toast.makeText(this, "Nota eliminada", Toast.LENGTH_SHORT).show();
                        finish(); // Cierra la actividad
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

    }

    private void eliminarNota(String key) {
        SharedPreferences prefs = getSharedPreferences("notas", MODE_PRIVATE);
        prefs.edit().remove(key).apply();

        SharedPreferences catPrefs = getSharedPreferences("notas_categorias", MODE_PRIVATE);
        catPrefs.edit().remove(key + "_categoria").apply();

        FavoritosManager.eliminarFavorito(this, key);
    }


    private void applyStyle(Object style) {
        int start = noteEditText.getSelectionStart();
        int end = noteEditText.getSelectionEnd();
        if (start == end) return;
        noteEditText.getText().setSpan(style, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void disableToolbar() {
        btnBold.setEnabled(false);
        btnItalic.setEnabled(false);
        btnUnderline.setEnabled(false);
        btnBullet.setEnabled(false);
        btnFavorito.setEnabled(false);
        categorySpinner.setEnabled(false);
    }

    private void saveNote(String key, Editable content) {
        SharedPreferences prefs = getSharedPreferences("notas", MODE_PRIVATE);
        String html = Html.toHtml(content, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);
        prefs.edit().putString(key, html).apply();
    }

    private Spanned loadFormattedNote(String key) {
        SharedPreferences prefs = getSharedPreferences("notas", MODE_PRIVATE);
        String html = prefs.getString(key, "");
        return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
    }

    private void saveCategoriaSeleccionada(String key) {
        SharedPreferences prefs = getSharedPreferences("notas_categorias", MODE_PRIVATE);
        String categoria = categorySpinner.getSelectedItem().toString();
        prefs.edit().putString(key + "_categoria", categoria).apply();
    }

    private void loadCategoriaSeleccionada(String key) {
        SharedPreferences prefs = getSharedPreferences("notas_categorias", MODE_PRIVATE);
        String categoria = prefs.getString(key + "_categoria", null);
        if (categoria != null) {
            ArrayAdapter adapter = (ArrayAdapter) categorySpinner.getAdapter();
            int position = adapter.getPosition(categoria);
            if (position >= 0) categorySpinner.setSelection(position);
        }
    }

    private void showExportDialog() {
        String[] opciones = {"Exportar como PDF", "Exportar como TXT"};

        new AlertDialog.Builder(this)
                .setTitle("Selecciona formato de exportación")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        exportarComoPDF();
                    } else {
                        exportarComoTXT();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String generarContenidoNota() {
        String fecha = "Fecha: " + selectedDate;
        String categoria = "Categoría: " + categorySpinner.getSelectedItem().toString();
        String contenido = Html.fromHtml(Html.toHtml(noteEditText.getText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL), Html.FROM_HTML_MODE_LEGACY).toString();
        return fecha + "\n" + categoria + "\n\n" + contenido;
    }

    private void exportarComoTXT() {
        String contenido = generarContenidoNota();
        try {
            File directorio = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "NotasExportadas");
            if (!directorio.exists()) directorio.mkdirs();

            String nombreArchivo = "Nota_" + selectedDate.replace("-", "_") + "_" + noteId + ".txt";
            File archivo = new File(directorio, nombreArchivo);

            FileWriter writer = new FileWriter(archivo);
            writer.write(contenido);
            writer.close();

            Toast.makeText(this, "TXT guardado en:\n" + archivo.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al exportar TXT: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void exportarComoPDF() {
        String contenido = generarContenidoNota();
        PdfDocument documento = new PdfDocument();
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page pagina = documento.startPage(info);

        Canvas canvas = pagina.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(14);

        int x = 30, y = 50;
        for (String linea : contenido.split("\n")) {
            canvas.drawText(linea, x, y, paint);
            y += 20;
        }

        documento.finishPage(pagina);

        try {
            File directorio = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "NotasExportadas");
            if (!directorio.exists()) directorio.mkdirs();

            String nombreArchivo = "Nota_" + selectedDate.replace("-", "_") + "_" + noteId + ".pdf";
            File archivo = new File(directorio, nombreArchivo);

            FileOutputStream fos = new FileOutputStream(archivo);
            documento.writeTo(fos);
            documento.close();
            fos.close();

            Toast.makeText(this, "PDF guardado en:\n" + archivo.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al exportar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


}
