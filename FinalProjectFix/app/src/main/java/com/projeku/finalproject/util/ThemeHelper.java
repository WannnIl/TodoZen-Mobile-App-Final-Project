package com.projeku.finalproject.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {

    private static final String PREFS_NAME = "ThemePrefs";
    private static final String KEY_THEME = "selected_theme";
    public static final int LIGHT_MODE = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int DARK_MODE = AppCompatDelegate.MODE_NIGHT_YES;
    public static final int FOLLOW_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

    public static void applyTheme(int theme) {
        AppCompatDelegate.setDefaultNightMode(theme);
    }

    public static void saveThemePreference(Context context, int theme) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(KEY_THEME, theme);
        editor.apply();
    }

    public static int loadThemePreference(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Default to system theme if no preference is saved
        return prefs.getInt(KEY_THEME, FOLLOW_SYSTEM);
    }
}
