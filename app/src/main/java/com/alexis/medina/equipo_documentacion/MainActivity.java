package com.alexis.medina.equipo_documentacion;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    CalendarView calendarView;
    Button btnSearch, btnFavoritos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendarView);
        btnSearch = findViewById(R.id.btnSearch);
        btnFavoritos = findViewById(R.id.btnFavoritos);

        // Abrir favoritos
        btnFavoritos.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoritosActivity.class);
            startActivity(intent);
        });

        // Buscar notas
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Al seleccionar fecha
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Selecciona una opción")
                    .setItems(new CharSequence[]{"Nueva nota", "Ver notas existentes"}, (dialog, which) -> {
                        if (which == 0) {
                            String id = UUID.randomUUID().toString();
                            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                            intent.putExtra("selectedDate", selectedDate);
                            intent.putExtra("noteId", selectedDate + "_" + id);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(MainActivity.this, NotesListActivity.class);
                            intent.putExtra("selectedDate", selectedDate);
                            startActivity(intent);
                        }
                    }).show();
        });
    }
}
