package com.cleantv.launcher.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class PrefUtils {
    private static final String PREF_NAME = "cleantv_prefs";
    private static final String KEY_FAVORITES = "key_favorites";
    private static final String KEY_HIDDEN = "key_hidden";
    private static final String KEY_FIRST_RUN = "key_first_run_filter";
    private static final String KEY_USER_GREETING = "key_user_greeting";

    // Khóa màn hình & Hẹn giờ
    private static final String KEY_LOCK_MINUTES = "key_lock_minutes";
    private static final String KEY_IS_LOCKED = "key_is_locked";
    private static final String KEY_LOCK_START_TIME = "key_lock_start_time";

    public static final String[] DEFAULT_VNPT_BLACKLIST = {
            "com.vnpt.smartbox.launcher",
            "com.vnpt.appstore",
            "com.vnpt.karaoke",
            "com.vnpt.education",
            "com.vnpt.smartbox.guide",
            "com.vnpt.smartbox.feedback",
            "com.vnpt.smartbox.cloud",
            "com.vnpt.smartbox.update",
            "com.vnpt.smartbox.setting",
            "com.vnpt.ott",
            "com.android.music",
            "com.android.soundrecorder",
            "com.android.email",
            "com.android.quicksearchbox",
            "com.android.development"
    };

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getUserGreeting(Context context) {
        return getPrefs(context).getString(KEY_USER_GREETING, "Xin chào, Vinhdh");
    }

    public static void setUserGreeting(Context context, String greeting) {
        getPrefs(context).edit().putString(KEY_USER_GREETING, greeting).apply();
    }

    // ==========================================
    // QUẢN LÝ HẸN GIỜ KHÓA MÀN HÌNH
    // ==========================================

    public static int getLockDurationMinutes(Context context) {
        return getPrefs(context).getInt(KEY_LOCK_MINUTES, 0); // 0 = Tắt
    }

    public static void setLockDurationMinutes(Context context, int minutes) {
        getPrefs(context).edit().putInt(KEY_LOCK_MINUTES, minutes).apply();
    }

    public static boolean isScreenLocked(Context context) {
        return getPrefs(context).getBoolean(KEY_IS_LOCKED, false);
    }

    public static void setScreenLocked(Context context, boolean locked) {
        getPrefs(context).edit().putBoolean(KEY_IS_LOCKED, locked).apply();
    }

    public static long getLockStartTime(Context context) {
        return getPrefs(context).getLong(KEY_LOCK_START_TIME, 0);
    }

    public static void setLockStartTime(Context context, long timeMillis) {
        getPrefs(context).edit().putLong(KEY_LOCK_START_TIME, timeMillis).apply();
    }

    /**
     * Kiểm tra mã bảo mật động theo thời gian thực (Giờ/Ngày/Tháng)
     * Ví dụ ngày 25/10:
     * - Lúc 13:xx -> 132510 hoặc 251013
     * - Lúc 14:xx -> 142510 hoặc 251014
     * Hỗ trợ cả mã khẩn cấp 888888 phòng khi TV bị lệch giờ.
     */
    public static boolean checkDynamicPasscode(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        String clean = input.trim();

        Date now = new Date();
        // Định dạng 1: Giờ + Ngày + Tháng (HHddMM) -> VD: 13h00 ngày 25/10 = 132510
        String codeHHddMM = new SimpleDateFormat("HHddMM", Locale.US).format(now);
        // Định dạng 2: Ngày + Tháng + Giờ (ddMMHH) -> VD: 14h30 ngày 25/10 = 251014
        String codeDdMMHH = new SimpleDateFormat("ddMMHH", Locale.US).format(now);

        // Chấp nhận cả 2 định dạng giờ và mã cứu hộ 888888
        return clean.equals(codeHHddMM) || clean.equals(codeDdMMHH) || clean.equals("888888");
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
        boolean isFirstRun = getPrefs(context).getBoolean(KEY_FIRST_RUN, true);
        if (isFirstRun) {
            Set<String> initialHidden = new HashSet<>(Arrays.asList(DEFAULT_VNPT_BLACKLIST));
            getPrefs(context).edit()
                    .putStringSet(KEY_HIDDEN, initialHidden)
                    .putBoolean(KEY_FIRST_RUN, false)
                    .apply();
            return initialHidden;
        }

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

    public static void restoreDefaultVnptFilter(Context context) {
        Set<String> initialHidden = new HashSet<>(Arrays.asList(DEFAULT_VNPT_BLACKLIST));
        getPrefs(context).edit().putStringSet(KEY_HIDDEN, initialHidden).apply();
    }
}
