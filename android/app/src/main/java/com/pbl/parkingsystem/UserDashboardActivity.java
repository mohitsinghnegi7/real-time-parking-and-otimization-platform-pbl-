package com.pbl.parkingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.pbl.parkingsystem.FindParkingActivity;
import com.pbl.parkingsystem.ProfileActivity;
import com.pbl.parkingsystem.dto.BookingResponse;
import com.pbl.parkingsystem.network.ApiService;
import com.pbl.parkingsystem.network.RetrofitClient;
import com.pbl.parkingsystem.util.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDashboardActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private ApiService apiService;

    private TextView tvUserName;

    private MaterialCardView actionVehicles;
    private MaterialCardView actionBookings;
    private MaterialCardView actionPayments;
    private MaterialCardView actionProfile;

    private MaterialCardView cardFindParking;
    private TextView btnFindParking;
    private MaterialCardView cardNearbyParking;
    private Button btnBookNow;

    // Active booking views
    private MaterialCardView cardActiveBooking;
    private TextView tvActiveBookingSlot;
    private TextView tvActiveBookingStatus;
    private TextView tvActiveBookingLot;
    private TextView tvActiveBookingTime;
    private TextView tvActiveBookingVehicle;
    private TextView tvNoActiveBooking;
    private TextView btnViewBooking;

    private Call<List<BookingResponse>> bookingsCall;
    private boolean isLoadingBooking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService(this);

        // User information
        tvUserName = findViewById(R.id.tvUserName);

        String name = tokenManager.getName();
        String token = tokenManager.getToken();
        String role = tokenManager.getRole();
        Long userId = tokenManager.getUserId();

        Log.d("TOKEN_TEST", "Name: " + name);
        Log.d("TOKEN_TEST", "User ID: " + userId);
        Log.d("TOKEN_TEST", "Role: " + role);
        Log.d("TOKEN_TEST", "Token exists: " + (token != null));

        if (name != null && !name.isEmpty()) {
            tvUserName.setText(name);
        }

        // Dashboard views
        actionVehicles = findViewById(R.id.actionVehicles);
        actionBookings = findViewById(R.id.actionBookings);
        actionPayments = findViewById(R.id.actionPayments);
        actionProfile = findViewById(R.id.actionProfile);

        cardFindParking = findViewById(R.id.cardFindParking);
        btnFindParking = findViewById(R.id.btnFindParking);
        cardNearbyParking = findViewById(R.id.cardNearbyParking);
        btnBookNow = findViewById(R.id.btnBookNow);

        // Active booking views
        cardActiveBooking = findViewById(R.id.cardActiveBooking);
        tvActiveBookingSlot = findViewById(R.id.tvActiveBookingSlot);
        tvActiveBookingStatus = findViewById(R.id.tvActiveBookingStatus);
        tvActiveBookingLot = findViewById(R.id.tvActiveBookingLot);
        tvActiveBookingTime = findViewById(R.id.tvActiveBookingTime);
        tvActiveBookingVehicle = findViewById(R.id.tvActiveBookingVehicle);
        tvNoActiveBooking = findViewById(R.id.tvNoActiveBooking);
        btnViewBooking = findViewById(R.id.btnViewBooking);

        // My Vehicles
        actionVehicles.setOnClickListener(v ->
                startActivity(new Intent(
                        UserDashboardActivity.this,
                        VehicleActivity.class
                ))
        );

        // My Bookings
        actionBookings.setOnClickListener(v -> openMyBookings());

        // Payments
        actionPayments.setOnClickListener(v ->
                Toast.makeText(
                        UserDashboardActivity.this,
                        "Payments clicked",
                        Toast.LENGTH_SHORT
                ).show()
        );

        // Profile
        actionProfile.setOnClickListener(v -> openProfile());

        // Find Parking
        cardFindParking.setOnClickListener(v -> openFindParking());
        btnFindParking.setOnClickListener(v -> openFindParking());

        // Book Now
        btnBookNow.setOnClickListener(v ->
                startActivity(new Intent(
                        UserDashboardActivity.this,
                        BookParkingActivity.class
                ))
        );

        // View booking
        btnViewBooking.setOnClickListener(v -> openMyBookings());

        // Bottom navigation
        findViewById(R.id.navProfile).setOnClickListener(v -> openProfile());

        findViewById(R.id.navVehicles).setOnClickListener(v ->
                startActivity(new Intent(
                        UserDashboardActivity.this,
                        VehicleActivity.class
                ))
        );

        findViewById(R.id.navBookings).setOnClickListener(v ->
                openMyBookings()
        );

        findViewById(R.id.navHome).setOnClickListener(v -> {
            // Already on Home
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActiveBooking();
    }

    private void loadActiveBooking() {

        if (isLoadingBooking) {
            return;
        }

        Long userId = tokenManager.getUserId();

        if (userId == null) {
            showNoActiveBooking(
                    "Please login again to view your booking."
            );
            return;
        }

        isLoadingBooking = true;

        cardActiveBooking.setVisibility(View.VISIBLE);
        tvNoActiveBooking.setVisibility(View.VISIBLE);
        tvNoActiveBooking.setText("Loading your booking...");

        bookingsCall = apiService.getUserBookings(userId);

        bookingsCall.enqueue(new Callback<List<BookingResponse>>() {

            @Override
            public void onResponse(
                    Call<List<BookingResponse>> call,
                    Response<List<BookingResponse>> response) {

                isLoadingBooking = false;

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(
                            "ACTIVE_BOOKING",
                            "HTTP error: " + response.code()
                    );

                    showNoActiveBooking(
                            "Unable to load booking. Please try again."
                    );
                    return;
                }

                List<BookingResponse> bookings = response.body();

                Log.d(
                        "ACTIVE_BOOKING",
                        "Bookings received: " + bookings.size()
                );

                BookingResponse latestBooking = null;

                for (BookingResponse booking : bookings) {

                    if (booking == null || booking.getStatus() == null) {
                        continue;
                    }

                    String status = booking.getStatus();

                    // Ignore cancelled and completed bookings
                    if (!"CONFIRMED".equalsIgnoreCase(status)
                            && !"PENDING".equalsIgnoreCase(status)) {
                        continue;
                    }

                    if (latestBooking == null) {
                        latestBooking = booking;
                    } else {
                        String currentStart = booking.getStartTime();
                        String latestStart = latestBooking.getStartTime();

                        if (currentStart != null
                                && (latestStart == null
                                || currentStart.compareTo(latestStart) > 0)) {
                            latestBooking = booking;
                        }
                    }
                }

                if (latestBooking == null) {
                    showNoActiveBooking(
                            "You don't have any active bookings yet."
                    );
                } else {
                    Log.d(
                            "ACTIVE_BOOKING",
                            "Showing booking ID: "
                                    + latestBooking.getBookingId()
                                    + ", status: "
                                    + latestBooking.getStatus()
                    );

                    displayActiveBooking(latestBooking);
                }
            }

            @Override
            public void onFailure(
                    Call<List<BookingResponse>> call,
                    Throwable t) {

                isLoadingBooking = false;

                if (isFinishing() || isDestroyed() || call.isCanceled()) {
                    return;
                }

                Log.e(
                        "ACTIVE_BOOKING",
                        "Request failed",
                        t
                );

                showNoActiveBooking(
                        "Network error. Please open My Bookings to check."
                );
            }
        });
    }

    private void displayActiveBooking(BookingResponse booking) {

        cardActiveBooking.setVisibility(View.VISIBLE);

        // Hide loading/empty message
        tvNoActiveBooking.setVisibility(View.GONE);

        // IMPORTANT: Make every booking field visible again
        tvActiveBookingSlot.setVisibility(View.VISIBLE);
        tvActiveBookingStatus.setVisibility(View.VISIBLE);
        tvActiveBookingLot.setVisibility(View.VISIBLE);
        tvActiveBookingVehicle.setVisibility(View.VISIBLE);
        tvActiveBookingTime.setVisibility(View.VISIBLE);
        btnViewBooking.setVisibility(View.VISIBLE);

        tvActiveBookingSlot.setText(
                "🅿️ Slot " + safe(booking.getSlotNumber())
        );

        tvActiveBookingStatus.setText(
                safe(booking.getStatus())
        );

        tvActiveBookingLot.setText(
                safe(booking.getParkingLotName())
        );

        tvActiveBookingVehicle.setText(
                "Vehicle: " + safe(booking.getVehicleNumber())
        );

        tvActiveBookingTime.setText(
                "Start: " + safe(booking.getStartTime())
                        + "\nEnd: " + safe(booking.getEndTime())
        );

        if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            tvActiveBookingStatus.setTextColor(0xFF2E7D32);
            tvActiveBookingStatus.setBackgroundColor(0xFFE8F5E9);
        } else {
            tvActiveBookingStatus.setTextColor(0xFF8A5A00);
            tvActiveBookingStatus.setBackgroundColor(0xFFFFF3CD);
        }
    }

    private void showNoActiveBooking(String message) {

        cardActiveBooking.setVisibility(View.VISIBLE);

        tvNoActiveBooking.setText(message);
        tvNoActiveBooking.setVisibility(View.VISIBLE);

        tvActiveBookingSlot.setVisibility(View.GONE);
        tvActiveBookingStatus.setVisibility(View.GONE);
        tvActiveBookingLot.setVisibility(View.GONE);
        tvActiveBookingTime.setVisibility(View.GONE);
        tvActiveBookingVehicle.setVisibility(View.GONE);

        btnViewBooking.setVisibility(View.VISIBLE);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty()
                ? "N/A"
                : value;
    }

    private void openMyBookings() {
        startActivity(new Intent(
                UserDashboardActivity.this,
                MyBookingsActivity.class
        ));
    }

    private void openFindParking() {
        startActivity(new Intent(
                UserDashboardActivity.this,
                FindParkingActivity.class
        ));
    }

    private void openProfile() {
        startActivity(new Intent(
                UserDashboardActivity.this,
                ProfileActivity.class
        ));
    }

    @Override
    protected void onDestroy() {
        if (bookingsCall != null) {
            bookingsCall.cancel();
        }

        super.onDestroy();
    }
}
