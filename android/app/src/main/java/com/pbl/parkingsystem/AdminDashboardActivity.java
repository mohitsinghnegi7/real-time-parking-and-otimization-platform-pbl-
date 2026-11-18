
package com.pbl.parkingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.pbl.parkingsystem.dto.PaymentAnalyticsResponse;
import com.pbl.parkingsystem.network.ApiService;
import com.pbl.parkingsystem.network.RetrofitClient;
import com.pbl.parkingsystem.util.TokenManager;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private ApiService apiService;

    // Payment analytics TextViews
    private TextView tvTodayPayments;
    private TextView tvTodayRevenue;

    private TextView tvYesterdayPayments;
    private TextView tvYesterdayRevenue;

    private TextView tvMonthPayments;
    private TextView tvMonthRevenue;

    private TextView tvTotalPayments;
    private TextView tvTotalRevenue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService(this);

        initializeViews();
        setupAdminName();
        setupClickListeners();
    }

    private void initializeViews() {

        // Header buttons and welcome text
        TextView tvAdminName = findViewById(R.id.tvAdminName);

        Button btnManageLots = findViewById(R.id.btnManageLots);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Today
        tvTodayPayments = findViewById(R.id.tvTodayPayments);
        tvTodayRevenue = findViewById(R.id.tvTodayRevenue);

        // Yesterday
        tvYesterdayPayments = findViewById(R.id.tvYesterdayPayments);
        tvYesterdayRevenue = findViewById(R.id.tvYesterdayRevenue);

        // This month
        tvMonthPayments = findViewById(R.id.tvMonthPayments);
        tvMonthRevenue = findViewById(R.id.tvMonthRevenue);

        // All time
        tvTotalPayments = findViewById(R.id.tvTotalPayments);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
    }

    private void setupAdminName() {

        TextView tvAdminName = findViewById(R.id.tvAdminName);

        String name = getIntent().getStringExtra("name");

        if (name == null || name.trim().isEmpty()) {
            name = tokenManager.getName();
        }

        if (name == null || name.trim().isEmpty()) {
            name = "Admin";
        }

        tvAdminName.setText("Welcome back, " + name + "!");
    }

    private void setupClickListeners() {

        Button btnManageLots = findViewById(R.id.btnManageLots);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnManageLots.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AdminDashboardActivity.this,
                    ParkingLotsActivity.class
            );

            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    // Refresh analytics whenever dashboard becomes visible
    @Override
    protected void onResume() {
        super.onResume();
        loadPaymentAnalytics();
    }

    private void loadPaymentAnalytics() {

        apiService.getPaymentAnalytics().enqueue(
                new Callback<PaymentAnalyticsResponse>() {

                    @Override
                    public void onResponse(
                            Call<PaymentAnalyticsResponse> call,
                            Response<PaymentAnalyticsResponse> response
                    ) {

                        if (response.isSuccessful() && response.body() != null) {

                            PaymentAnalyticsResponse analytics = response.body();

                            updatePaymentAnalytics(analytics);

                        } else {

                            Toast.makeText(
                                    AdminDashboardActivity.this,
                                    "Unable to load payment analytics. Code: "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PaymentAnalyticsResponse> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                AdminDashboardActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void updatePaymentAnalytics(
            PaymentAnalyticsResponse analytics
    ) {

        NumberFormat currencyFormat =
                NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        NumberFormat numberFormat =
                NumberFormat.getIntegerInstance(new Locale("en", "IN"));

        // Today's payments
        tvTodayPayments.setText(
                numberFormat.format(analytics.getTodayPayments())
        );

        tvTodayRevenue.setText(
                currencyFormat.format(analytics.getTodayRevenue())
        );

        // Yesterday's payments
        tvYesterdayPayments.setText(
                numberFormat.format(analytics.getYesterdayPayments())
        );

        tvYesterdayRevenue.setText(
                currencyFormat.format(analytics.getYesterdayRevenue())
        );

        // This month's payments
        tvMonthPayments.setText(
                numberFormat.format(analytics.getThisMonthPayments())
        );

        tvMonthRevenue.setText(
                currencyFormat.format(analytics.getThisMonthRevenue())
        );

        // Total payments
        tvTotalPayments.setText(
                numberFormat.format(analytics.getTotalPayments())
        );

        tvTotalRevenue.setText(
                currencyFormat.format(analytics.getTotalRevenue())
        );
    }

    private void showLogoutConfirmation() {

        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setNegativeButton("Cancel", (dialog, which) ->
                        dialog.dismiss())
                .setPositiveButton("Logout", (dialog, which) ->
                        logout())
                .show();
    }

    private void logout() {

        tokenManager.clearToken();

        Intent intent = new Intent(
                AdminDashboardActivity.this,
                MainActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}
