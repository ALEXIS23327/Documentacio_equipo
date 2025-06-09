package com.alexis.medina.equipo_documentacion;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class FavoritosManager {

    private static final String PREF_NAME = "favoritos";
    private static final String KEY_LIST = "listaFavoritos";

    public static void guardarFavorito(Context context, String noteId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> favoritos = new HashSet<>(prefs.getStringSet(KEY_LIST, new HashSet<>()));
        favoritos.add(noteId);
        prefs.edit().putStringSet(KEY_LIST, favoritos).apply();
    }

    public static void eliminarFavorito(Context context, String noteId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> favoritos = new HashSet<>(prefs.getStringSet(KEY_LIST, new HashSet<>()));
        favoritos.remove(noteId);
        prefs.edit().putStringSet(KEY_LIST, favoritos).apply();
    }

    public static boolean esFavorito(Context context, String noteId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> favoritos = prefs.getStringSet(KEY_LIST, new HashSet<>());
        return favoritos.contains(noteId);
    }

    public static Set<String> obtenerFavoritos(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(KEY_LIST, new HashSet<>());
    }
}
