package com.pbl.parkingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pbl.parkingsystem.util.TokenManager;
import com.pbl.parkingsystem.dto.LoginRequest;
import com.pbl.parkingsystem.dto.LoginResponse;
import com.pbl.parkingsystem.network.ApiService;
import com.pbl.parkingsystem.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private TokenManager tokenManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        apiService = RetrofitClient.getApiService(this);
        tokenManager = new TokenManager(this);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    SignupActivity.class
            );
            startActivity(intent);
        });
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        LoginRequest request = new LoginRequest(email, password);

        btnLogin.setEnabled(false);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(
                    Call<LoginResponse> call,
                    Response<LoginResponse> response) {

                btnLogin.setEnabled(true);

                if (response.isSuccessful()
                        && response.body() != null) {

                    LoginResponse loginResponse = response.body();

                    String name = loginResponse.getName();
                    String role = loginResponse.getRole();
                    String token = loginResponse.getToken();
                    Long userId = loginResponse.getId();

                    if (role == null || role.trim().isEmpty()) {
                        Toast.makeText(
                                MainActivity.this,
                                "User role not received from server",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    if (!"USER".equalsIgnoreCase(role)
                            && !"ADMIN".equalsIgnoreCase(role)) {

                        Toast.makeText(
                                MainActivity.this,
                                "Unsupported role: " + role,
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    // Save login information
                    tokenManager.saveToken(token);
                    tokenManager.saveName(name);
                    tokenManager.saveRole(role);
                    tokenManager.saveUserId(userId);
                    tokenManager.saveEmail(email);

                    Toast.makeText(
                            MainActivity.this,
                            "Welcome " + name,
                            Toast.LENGTH_SHORT
                    ).show();

                    if ("ADMIN".equalsIgnoreCase(role)) {

                        // Open Admin Dashboard
                        Intent intent = new Intent(
                                MainActivity.this,
                                AdminDashboardActivity.class
                        );

                        intent.putExtra("name", name);
                        startActivity(intent);
                        finish();

                    } else {

                        // Existing User Dashboard
                        Intent intent = new Intent(
                                MainActivity.this,
                                UserDashboardActivity.class
                        );

                        startActivity(intent);
                        finish();
                    }

                } else {

                    Toast.makeText(
                            MainActivity.this,
                            "Login failed. Error code: "
                                    + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<LoginResponse> call,
                    Throwable t) {

                btnLogin.setEnabled(true);

                Toast.makeText(
                        MainActivity.this,
                        "Unable to connect to server:\n"
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}

