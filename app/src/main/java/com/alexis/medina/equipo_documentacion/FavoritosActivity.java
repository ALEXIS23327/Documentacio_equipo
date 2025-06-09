package com.alexis.medina.equipo_documentacion;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;

public class FavoritosActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> notaIds = new ArrayList<>();
    ArrayList<String> titulos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);

        listView = findViewById(R.id.listViewFavoritos);

        Set<String> favoritos = FavoritosManager.obtenerFavoritos(this);
        SharedPreferences prefs = getSharedPreferences("notas", Context.MODE_PRIVATE);

        for (String id : favoritos) {
            String html = prefs.getString(id, "");
            Spanned texto = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
            String contenido = texto.toString().split("\n")[0]; // primera línea = título
            titulos.add(contenido.isEmpty() ? "(Sin título)" : contenido);
            notaIds.add(id);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titulos);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            Intent intent = new Intent(FavoritosActivity.this, NoteActivity.class);
            String fullId = notaIds.get(position);
            String[] parts = fullId.split("_");
            String selectedDate = parts[0];

            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("noteId", fullId);
            startActivity(intent);
        });
    }
}
