
package com.pbl.parkingsystem.util;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static final String PREF_NAME = "ParkingAppPrefs";

    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "user_email";

    private final SharedPreferences preferences;

    public TokenManager(Context context) {
        preferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    public void saveToken(String token) {
        preferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public void saveName(String name) {
        preferences.edit().putString(KEY_NAME, name).apply();
    }

    public String getName() {
        return preferences.getString(KEY_NAME, null);
    }

    public void saveRole(String role) {
        preferences.edit().putString(KEY_ROLE, role).apply();
    }

    public String getRole() {
        return preferences.getString(KEY_ROLE, null);
    }

    public void saveUserId(Long userId) {
        if (userId != null) {
            preferences.edit().putLong(KEY_USER_ID, userId).apply();
        } else {
            preferences.edit().remove(KEY_USER_ID).apply();
        }
    }

    public Long getUserId() {
        long userId = preferences.getLong(KEY_USER_ID, -1);
        return userId == -1 ? null : userId;
    }

    public void saveEmail(String email) {
        preferences.edit().putString(KEY_EMAIL, email).apply();
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, null);
    }

    public void clearToken() {
        preferences.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_NAME)
                .remove(KEY_ROLE)
                .remove(KEY_USER_ID)
                .remove(KEY_EMAIL)
                .apply();
    }
}
