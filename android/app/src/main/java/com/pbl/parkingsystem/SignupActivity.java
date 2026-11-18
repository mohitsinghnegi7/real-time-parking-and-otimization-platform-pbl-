package com.pbl.parkingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.pbl.parkingsystem.dto.UserRegistrationRequest;
import com.pbl.parkingsystem.dto.UserResponse;
import com.pbl.parkingsystem.network.ApiService;
import com.pbl.parkingsystem.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;

    private Button btnSignup;
    private TextView tvLogin;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);

        // Connect XML views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnSignup = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);

        // Retrofit
        apiService = RetrofitClient.getApiService(this);

        // Signup button
        btnSignup.setOnClickListener(v -> registerUser());

        // Login text
        tvLogin.setOnClickListener(v -> {

            Intent intent = new Intent(
                    SignupActivity.this,
                    MainActivity.class
            );

            startActivity(intent);

            finish();
        });
    }

    private void registerUser() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etName.setError("Please enter your name");
            etName.requestFocus();
            return;
        }

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

        if (password.length() < 6) {
            etPassword.setError(
                    "Password must contain at least 6 characters"
            );
            etPassword.requestFocus();
            return;
        }

        // Create request
        UserRegistrationRequest request =
                new UserRegistrationRequest(
                        name,
                        email,
                        password
                );

        // Disable button
        btnSignup.setEnabled(false);

        // Call backend
        apiService.registerUser(request)
                .enqueue(new Callback<UserResponse>() {

                    @Override
                    public void onResponse(
                            Call<UserResponse> call,
                            Response<UserResponse> response) {

                        btnSignup.setEnabled(true);

                        if (response.isSuccessful()
                                && response.body() != null) {

                            UserResponse user =
                                    response.body();

                            Toast.makeText(
                                    SignupActivity.this,
                                    "Account created successfully!",
                                    Toast.LENGTH_LONG
                            ).show();

                            // Go to Login
                            Intent intent = new Intent(
                                    SignupActivity.this,
                                    MainActivity.class
                            );

                            startActivity(intent);
                            finish();

                        } else {

                            Toast.makeText(
                                    SignupActivity.this,
                                    "Registration failed. Error code: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<UserResponse> call,
                            Throwable t) {




                        btnSignup.setEnabled(true);

                        Log.e("SIGNUP", "API FAILED", t);

                        Toast.makeText(
                                SignupActivity.this,
                                "ERROR: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}