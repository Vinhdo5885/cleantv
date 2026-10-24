package com.cleantv.launcher.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class PrefUtils {
    private static final String PREF_NAME = "cleantv_prefs";
    private static final String KEY_FAVORITES = "key_favorites";
    private static final String KEY_HIDDEN = "key_hidden";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static Set<String> getFavorites(Context context) {
        Set<String> set = getPrefs(context).getStringSet(KEY_FAVORITES, null);
        return set != null ? new HashSet<>(set) : new HashSet<String>();
    }

    public static void addFavorite(Context context, String packageName) {
        Set<String> set = getFavorites(context);
        set.add(packageName);
        getPrefs(context).edit().putStringSet(KEY_FAVORITES, set).apply();
    }

    public static void removeFavorite(Context context, String packageName) {
        Set<String> set = getFavorites(context);
        set.remove(packageName);
        getPrefs(context).edit().putStringSet(KEY_FAVORITES, set).apply();
    }

    public static boolean isFavorite(Context context, String packageName) {
        return getFavorites(context).contains(packageName);
    }

    public static Set<String> getHiddenApps(Context context) {
        Set<String> set = getPrefs(context).getStringSet(KEY_HIDDEN, null);
        return set != null ? new HashSet<>(set) : new HashSet<String>();
    }

    public static void hideApp(Context context, String packageName) {
        Set<String> set = getHiddenApps(context);
        set.add(packageName);
        getPrefs(context).edit().putStringSet(KEY_HIDDEN, set).apply();
    }

    public static void unhideAll(Context context) {
        getPrefs(context).edit().remove(KEY_HIDDEN).apply();
    }
}
