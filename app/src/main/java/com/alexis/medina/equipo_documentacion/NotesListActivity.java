package com.alexis.medina.equipo_documentacion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Map;

public class NotesListActivity extends AppCompatActivity {

    ListView listView;
    String selectedDate;
    ArrayList<String> keys = new ArrayList<>();
    ArrayList<String> previews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listView = new ListView(this);
        setContentView(listView);

        selectedDate = getIntent().getStringExtra("selectedDate");

        SharedPreferences prefs = getSharedPreferences("notas", MODE_PRIVATE);
        Map<String, ?> all = prefs.getAll();

        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getKey().startsWith(selectedDate)) {
                keys.add(entry.getKey());
                String html = entry.getValue().toString();
                String preview = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
                previews.add(preview.length() > 40 ? preview.substring(0, 40) + "..." : preview);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, previews);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String noteKey = keys.get(position);
            Intent intent = new Intent(NotesListActivity.this, NoteActivity.class);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("noteId", noteKey);
            startActivity(intent);
        });
    }
}