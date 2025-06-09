package com.alexis.medina.equipo_documentacion;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteActivity extends AppCompatActivity {

    TextView dateView;
    EditText noteEditText;
    Button saveButton, btnBold, btnItalic, btnUnderline, btnBullet, btnFavorito;
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
}