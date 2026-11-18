
package com.pbl.parkingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.pbl.parkingsystem.network.ApiService;
import com.pbl.parkingsystem.network.RetrofitClient;
import com.pbl.parkingsystem.util.TokenManager;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileInitial;
    private TextView tvProfileName;
    private TextView tvProfileRole;
    private TextView tvProfileEmail;
    private TextView tvNameDetail;

    private Button btnLogout;
    private ImageButton btnBack;

    private TokenManager tokenManager;
    private ApiService apiService;

    private boolean isLoggingOut = false;
    private Call<Map<String, String>> logoutCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService(this);

        tvProfileInitial = findViewById(R.id.tvProfileInitial);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvNameDetail = findViewById(R.id.tvNameDetail);

        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        loadProfile();

        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void loadProfile() {

        String name = tokenManager.getName();
        String role = tokenManager.getRole();
        String email = tokenManager.getEmail();

        // Display name
        if (name != null && !name.trim().isEmpty()) {
            tvProfileName.setText(name);
            tvNameDetail.setText(name);

            tvProfileInitial.setText(
                    name.trim().substring(0, 1).toUpperCase()
            );
        } else {
            tvProfileName.setText("User");
            tvNameDetail.setText("Not available");
            tvProfileInitial.setText("U");
        }

        // Display role
        if (role != null && !role.trim().isEmpty()) {
            tvProfileRole.setText(role);
        } else {
            tvProfileRole.setText("USER");
        }

        // Display email
        if (email != null && !email.trim().isEmpty()) {
            tvProfileEmail.setText(email);
        } else {
            tvProfileEmail.setText("Not available");
        }
    }

    private void showLogoutConfirmation() {

        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setNegativeButton("Cancel", (dialog, which) ->
                        dialog.dismiss()
                )
                .setPositiveButton("Logout", (dialog, which) ->
                        performLogout()
                )
                .show();
    }

    private void performLogout() {

        if (isLoggingOut) {
            return;
        }

        isLoggingOut = true;
        btnLogout.setEnabled(false);
        btnLogout.setText("Logging out...");

        String token = tokenManager.getToken();

        // If no token exists, clear local session immediately.
        if (token == null || token.trim().isEmpty()) {
            finishLocalLogout();
            return;
        }

        logoutCall = apiService.logout();

        logoutCall.enqueue(new Callback<Map<String, String>>() {

            @Override
            public void onResponse(
                    Call<Map<String, String>> call,
                    Response<Map<String, String>> response) {

                // Clear local session regardless of HTTP response.
                finishLocalLogout();
            }

            @Override
            public void onFailure(
                    Call<Map<String, String>> call,
                    Throwable t) {

                // Network failure should not prevent local logout.
                finishLocalLogout();
            }
        });
    }

    private void finishLocalLogout() {

        tokenManager.clearToken();

        Toast.makeText(
                ProfileActivity.this,
                "Logged out successfully",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(
                ProfileActivity.this,
                MainActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (logoutCall != null) {
            logoutCall.cancel();
        }

        super.onDestroy();
    }
}
