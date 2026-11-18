
package com.pbl.parkingsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pbl.parkingsystem.dto.BookingResponse;
import com.pbl.parkingsystem.network.ApiService;
import com.pbl.parkingsystem.network.RetrofitClient;
import com.pbl.parkingsystem.util.TokenManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBookingsActivity extends AppCompatActivity {

    private LinearLayout bookingsContainer;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private Button btnRefresh;

    private ApiService apiService;
    private TokenManager tokenManager;

    private boolean isLoading = false;
    private Call<List<BookingResponse>> bookingsCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        bookingsContainer = findViewById(R.id.bookingsContainer);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnRefresh = findViewById(R.id.btnRefresh);

        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnRefresh.setOnClickListener(v -> loadBookings());

        // Initial request only once.
        loadBookings();
    }

    @Override
    protected void onDestroy() {
        if (bookingsCall != null) {
            bookingsCall.cancel();
        }
        super.onDestroy();
    }

    private void loadBookings() {

        // Prevent simultaneous duplicate requests.
        if (isLoading) {
            return;
        }

        Long userId = tokenManager.getUserId();

        if (userId == null) {
            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        isLoading = true;
        btnRefresh.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // Clear old cards before displaying the latest response.
        bookingsContainer.removeAllViews();

        bookingsCall = apiService.getUserBookings(userId);

        bookingsCall.enqueue(new Callback<List<BookingResponse>>() {

            @Override
            public void onResponse(
                    Call<List<BookingResponse>> call,
                    Response<List<BookingResponse>> response) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                isLoading = false;
                progressBar.setVisibility(View.GONE);
                btnRefresh.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {

                    List<BookingResponse> bookings = response.body();

                    if (bookings.isEmpty()) {
                        showEmpty("You haven't booked any parking slots yet.");
                        return;
                    }

                    // Avoid displaying the same booking ID more than once.
                    Set<Long> displayedBookingIds = new HashSet<>();

                    for (BookingResponse booking : bookings) {

                        if (booking == null) {
                            continue;
                        }

                        Long bookingId = booking.getBookingId();

                        if (bookingId != null
                                && !displayedBookingIds.add(bookingId)) {
                            continue;
                        }

                        addBookingCard(booking);
                    }

                    if (bookingsContainer.getChildCount() == 0) {
                        showEmpty("No bookings found.");
                    }

                } else {
                    showEmpty("Unable to load bookings. Please refresh.");
                    Toast.makeText(
                            MyBookingsActivity.this,
                            "Request failed: HTTP " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<List<BookingResponse>> call,
                    Throwable t) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                isLoading = false;
                progressBar.setVisibility(View.GONE);
                btnRefresh.setEnabled(true);

                if (call.isCanceled()) {
                    return;
                }

                showEmpty("Network error. Please check your connection.");

                Toast.makeText(
                        MyBookingsActivity.this,
                        "Could not load bookings.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void addBookingCard(BookingResponse booking) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackgroundColor(0xFFFFFFFF);
        card.setElevation(dp(2));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(cardParams);

        addText(
                card,
                safe(booking.getParkingLotName()),
                19,
                0xFF171A21,
                true
        );

        addText(
                card,
                "Booking ID: " + safeId(booking.getBookingId()),
                13,
                0xFF737987,
                false
        );

        addText(
                card,
                "Slot: " + safe(booking.getSlotNumber()),
                15,
                0xFF343A46,
                true
        );

        addText(
                card,
                "Vehicle: " + safe(booking.getVehicleNumber()),
                14,
                0xFF555B68,
                false
        );

        addText(
                card,
                "Start: " + safe(booking.getStartTime()),
                14,
                0xFF555B68,
                false
        );

        addText(
                card,
                "End: " + safe(booking.getEndTime()),
                14,
                0xFF555B68,
                false
        );

        String status = safe(booking.getStatus());

        int statusColor;

        if ("CONFIRMED".equalsIgnoreCase(status)) {
            statusColor = 0xFF16794B;
        } else if ("CANCELLED".equalsIgnoreCase(status)) {
            statusColor = 0xFFB42318;
        } else {
            statusColor = 0xFF8A5A00;
        }

        addText(
                card,
                "Status: " + status,
                14,
                statusColor,
                true
        );

        if ("CONFIRMED".equalsIgnoreCase(status)
                || "PENDING".equalsIgnoreCase(status)) {

            Button btnCancel = new Button(this);
            btnCancel.setText("Cancel Booking");
            btnCancel.setAllCaps(false);

            btnCancel.setOnClickListener(v ->
                    cancelBooking(booking, btnCancel)
            );

            card.addView(btnCancel);
        }

        bookingsContainer.addView(card);
    }

    private void cancelBooking(
            BookingResponse booking,
            Button button) {

        Long userId = tokenManager.getUserId();

        if (userId == null || booking.getBookingId() == null) {
            Toast.makeText(
                    this,
                    "Unable to identify booking.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        button.setEnabled(false);

        apiService.cancelBooking(
                booking.getBookingId(),
                userId
        ).enqueue(new Callback<BookingResponse>() {

            @Override
            public void onResponse(
                    Call<BookingResponse> call,
                    Response<BookingResponse> response) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (response.isSuccessful()) {
                    Toast.makeText(
                            MyBookingsActivity.this,
                            "Booking cancelled.",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadBookings();
                } else {
                    button.setEnabled(true);

                    Toast.makeText(
                            MyBookingsActivity.this,
                            "Cancellation failed: HTTP " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<BookingResponse> call,
                    Throwable t) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                button.setEnabled(true);

                Toast.makeText(
                        MyBookingsActivity.this,
                        "Network error while cancelling booking.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showEmpty(String message) {
        bookingsContainer.removeAllViews();
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private void addText(
            LinearLayout parent,
            String value,
            int size,
            int color,
            boolean bold) {

        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextSize(size);
        textView.setTextColor(color);

        if (bold) {
            textView.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }

        textView.setPadding(0, dp(4), 0, dp(4));
        parent.addView(textView);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty()
                ? "N/A"
                : value;
    }

    private String safeId(Long value) {
        return value == null ? "N/A" : String.valueOf(value);
    }

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }
}
