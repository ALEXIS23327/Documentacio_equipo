package com.alexis.medina.equipo_documentacion;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    EditText searchInput;
    ListView resultsList;
    List<String> resultadosKeys;
    List<String> resultadosTitulos;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchInput = findViewById(R.id.searchInput);
        resultsList = findViewById(R.id.resultsList);

        resultadosKeys = new ArrayList<>();
        resultadosTitulos = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, resultadosTitulos);
        resultsList.setAdapter(adapter);

        // Búsqueda automática al escribir
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchInput.getText().toString().trim();
            buscarNotas(query);
            return true;
        });

        resultsList.setOnItemClickListener((parent, view, position, id) -> {
            String noteId = resultadosKeys.get(position);
            Intent intent = new Intent(this, NoteActivity.class);
            intent.putExtra("noteId", noteId);
            intent.putExtra("selectedDate", noteId);  // opcional, si usas date como clave
            startActivity(intent);
        });
    }

    private void buscarNotas(String query) {
        resultadosKeys.clear();
        resultadosTitulos.clear();

        SharedPreferences prefs = getSharedPreferences("notas", Context.MODE_PRIVATE);
        Map<String, ?> notas = prefs.getAll();

        for (Map.Entry<String, ?> entry : notas.entrySet()) {
            String key = entry.getKey();

            // Evita claves auxiliares como "nota123_categoria"
            if (key.endsWith("_categoria")) continue;

            String htmlContent = entry.getValue().toString();
            String plainText = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY).toString();

            if (plainText.toLowerCase().contains(query.toLowerCase())) {
                resultadosKeys.add(key);
                String titulo = plainText.split("\n")[0];
                resultadosTitulos.add(titulo.isEmpty() ? "(Sin título)" : titulo);
            }
        }

        if (resultadosKeys.isEmpty()) {
            Toast.makeText(this, "No se encontraron notas", Toast.LENGTH_SHORT).show();
        }

        adapter.notifyDataSetChanged();
    }
}
